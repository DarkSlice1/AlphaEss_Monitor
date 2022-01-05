package api.ember

import api.common.FileIO._
import api.common.Token
import api.ember.EmberObjectMapper._
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.TagName
import com.typesafe.config.Config
import metrics.KamonMetrics

import java.io.File
import java.time.{Instant, LocalDateTime, ZoneId}
import java.util
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


class ember(config: Config, reporterKamon : KamonMetrics) {


  val username = config.getString("ember.username")
  val password = config.getString("ember.password")
  val eplBaseHost = "https://eu-https.topband-cloud.com/ember-back/"
  var token = Token.empty()

  var gatewayId = ""

  def Run(): Unit = {

    token match {
      // No - empty token object returned
      case token if (token.AccessToken == "") => Login(); Run;
      // Yes
      case token =>
        val expiry = LocalDateTime.ofInstant(token.TokenCreateTime.toInstant, ZoneId.of("UTC")).plusSeconds(token.ExpiresIn.toLong)
        val today = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))
        if (today.isAfter(expiry)) {
          //token expired - lets refresh
          println("Ember Token is Expired")
          //refreshToken()
          //TODO figure out fresh token command
          Login()
          Run()
        }
        else {
          if (gatewayId.isEmpty)
            GetGatewayId()

          getMetrics()
        }
    }
  }


  def Login(): LoginReply = {
    val urlExtension = "appLogin/login"
    val reply = restCaller.simpleRestPostCall(eplBaseHost + urlExtension, LoginDetails(userName = username, password = password), "eu-https.topband-cloud.com")
    val result: LoginReply = jsonMapper.readValue(reply, classOf[LoginReply])

    result.data match {
      case null => println("ERROR : " + result.toString)
      case value: Any => token =  new Token(AccessToken = value.token, RefreshTokenKey = value.refresh_token)
    }
    result
  }

  def GetGatewayId() = {
    val urlExtension = "homes/list"
    val reply = restCaller.simpleRestGetCall(eplBaseHost + urlExtension,
      withToken = true,
      token = token.AccessToken,
      hostname = "eu-https.topband-cloud.com")

    gatewayId = (jsonMapper.readValue(reply, classOf[GatewayReply]).data.head.gatewayid)
    println("Ember Gateway Received")
  }

  def GetValueAtIndex(elements: HomeMetricsData, position: Int) : String = {

    ("" /: elements.pointDataList) { (RunningData, thisData) => {
      RunningData + thisData.pointIndex match {
        case x if x == ""+position => return thisData.value + ""
        case _ => ""
      }
    }
    }
  }

  def getMetrics(): Unit = {
    val urlExtension = "homesVT/zoneProgram"
    val reply = restCaller.simpleRestPostCall(eplBaseHost + urlExtension,
      GatewayID(gatewayId),
      withToken = true,
      token = token.AccessToken,
      hostname = "eu-https.topband-cloud.com")

    val mapper = (jsonMapper.readValue(reply, classOf[HomeMetrics]))

    val parsedResult = {
      (ListBuffer[HomeMetricsKeyData]() /: mapper.data) { (sum, element) => {
        val currentTemp = Integer.parseInt(GetValueAtIndex(element, 5))
        val isBoosting = if (GetValueAtIndex(element, 8) == "1") true else false
        val isBurning0il = if (GetValueAtIndex(element, 10) == "2") true else false
        sum += new HomeMetricsKeyData(element.name, currentTemp, isBoosting, isBurning0il)
      }
      }

      //flatten the tempAndBoost to a tuple


      //        sum += zoneName -> (if(tempAndBoost.size == 2)
      //          {
      //            Tuple2(
      //              tempAndBoost.find(_._1 != 0).getOrElse(0, false)._1,
      //              tempAndBoost.find(_._2 == true).getOrElse(
      //                tempAndBoost.find(_._1 != 0).getOrElse(0, false)._1, false)._2)
      //          }
      //            else
      //            {
      //              Tuple2(tempAndBoost.head._1, tempAndBoost.head._2)
      //          })
    }
      //}
    //}
    //we now have the zone name and the Temperature
    println(parsedResult)

    var isBurningOil = false
    parsedResult foreach (zone => {
      reporterKamon.emberTemperature.set(zone.CurrentTemp, "zone", zone.Zone)
      if(zone.IsBoost == true){
        reporterKamon.emberBoostCounter.increment(1,"zone", zone.Zone)
        reporterKamon.emberBoostGauge.set(1, "zone", zone.Zone)
      }
      else{
        reporterKamon.emberBoostGauge.set(0, "zone", zone.Zone)
      }
      if(zone.IsBurning == true) {
        //if boiler is on, it could be heating many zone - so lets not link to a zone
        isBurningOil = true
      }
    })

    if(isBurningOil){
      reporterKamon.emberBurnCounter.increment(1, "","")
      reporterKamon.emberBurnGauge.set(1)
    }
    else{
      reporterKamon.emberBurnGauge.set(0)
    }

  }


}