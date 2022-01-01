package api.ember

import api.common.FileIO._
import api.common.Token
import api.ember.EmberObjectMapper._
import com.typesafe.config.Config
import metrics.KamonMetrics

import java.io.File
import java.time.{Instant, LocalDateTime, ZoneId}
import scala.collection.mutable


class ember(config: Config, reporterKamon : KamonMetrics) {


  val username = config.getString("ember.username")
  val password = config.getString("ember.password")
  val eplBaseHost = "https://eu-https.topband-cloud.com/ember-back/"
  val tokenFile = new File(getClass.getResource("/EmberLastToken.txt").getFile())


  var gatewayId = ""

  def Run(): Unit = {

    readToken(tokenFile) match {
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
      case value: Any => writeToken(tokenFile, Some(new Token(AccessToken = value.token, RefreshTokenKey = value.refresh_token)))
    }
    result
  }

  def GetGatewayId() = {
    val urlExtension = "homes/list"
    val token = readToken(tokenFile)
    val reply = restCaller.simpleRestGetCall(eplBaseHost + urlExtension,
      withToken = true,
      token = token.AccessToken,
      hostname = "eu-https.topband-cloud.com")

    gatewayId = (jsonMapper.readValue(reply, classOf[GatewayReply]).data.head.gatewayid)
    println("Ember Gateway Received")
  }

  def getMetrics(): Unit = {
    val urlExtension = "homesVT/zoneProgram"
    val token = readToken(tokenFile)
    val reply = restCaller.simpleRestPostCall(eplBaseHost + urlExtension,
      GatewayID(gatewayId),
      withToken = true,
      token = token.AccessToken,
      hostname = "eu-https.topband-cloud.com")

    val mapper = (jsonMapper.readValue(reply, classOf[HomeMetrics]))

    val parsedResult = {
      (mutable.Map[String, (Int, Boolean)]() /: mapper.data) { (sum, element) => {
        val zoneName = element.name //Name of Zone
        //now get temperature of Zone

        var currentTemp = 0
        var isBoostOn = false
        val tempAndBoost = (mutable.Map[Int, Boolean]() /: element.pointDataList) { (pointSum, pointElement) => {

          pointElement.pointIndex match {

            //index 5 is the temp reading
            case 5 => {
                try {
                  currentTemp = Integer.parseInt(pointElement.value)
                }
                catch {
                  case ex: Exception => println("ERROR Running - Reading Temperature: " + ex.toString)
                }
            }

            //is Boost on?
            case 8 => {
              if(pointElement.value == "1")
                isBoostOn = true
            }

            case _ => //do nothing
          }
          pointSum += currentTemp -> isBoostOn
        }
        }

        //flatten the tempAndBoost to a tuple


        sum += zoneName -> (if(tempAndBoost.size == 2)
          {
            Tuple2(
              tempAndBoost.find(_._1 != 0).getOrElse(0, false)._1,
              tempAndBoost.find(_._2 == true).getOrElse(
                tempAndBoost.find(_._1 != 0).getOrElse(0, false)._1, false)._2)
          }
            else
            {
              Tuple2(tempAndBoost.head._1, tempAndBoost.head._2)
          })
      }
      }
    }
    //we now have the zone name and the Temperature
    println(parsedResult)

    parsedResult foreach (zone =>{
      reporterKamon.emberTemperature.set(zone._2._1, "zone", zone._1)
      if(zone._2._2 == true){
        //boost active
        reporterKamon.emberBoostCounter.increment(1,"zone", zone._1)
        reporterKamon.emberBoostGauge.set(1, "zone", zone._1)
      }
      else{
        reporterKamon.emberBoostGauge.set(0, "zone", zone._1)
      }

    })


  }


}