package api.alpha

import api.alpha.AlphaObjectMapper._
import api.common.FileIO._
import api.common.Token
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import metrics.KamonMetrics
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import java.time.{Instant, LocalDateTime, ZoneId}
import java.util

class alpha(config: Config, reporterKamon : KamonMetrics) extends LazyLogging{


  val username = config.getString("alphaess.username")
  val password = config.getString("alphaess.password")
  val sys_sn = config.getString("alphaess.system_sn")

  val reporter = new reportHome(config,reporterKamon)
  val eplBaseHost = "https://cloud.alphaess.com"

  var token = Token.empty()
  var currentBatteryPercentage = 0.0
  var currentGridPull = 0.0

  def run(): Unit = {
    //Do we have an access token
    token match {
      // No - empty token object returned
      case token if (token.AccessToken == "") => Login(); run;
      // Yes
      case token =>
        //val expiry = LocalDateTime.ofInstant(token.TokenCreateTime.toInstant,ZoneId.of("GMT")).plusSeconds(token.ExpiresIn.toLong)
        //val today = LocalDateTime.ofInstant(Instant.now(),ZoneId.of("GMT"))
        //if(today.isAfter(expiry)){
          //token expired - lets refresh
          //logger.info("Alpha Token is Expired")
          //refreshToken()
         //TODO figure out fresh token command
         // Login();
        //}
        //else{
          getMetrics()
        //}
    }
  }

  def Login():LoginReply ={
    val urlExtension= "/api/Account/Login"
    val reply = restCaller.simpleRestPostCall(eplBaseHost+urlExtension,new LoginDetails(username,password))
    val result: LoginReply = jsonMapper.readValue(reply, classOf[LoginReply])

    result.data match {
      case null => logger.error("ERROR : " + result.toString)
      case value : Any =>  token = value
    }
    result
  }

  def refreshToken(): Token = {
   // logger.info("Calling refreshToken()")
   // val urlExtension= "Api/Account/Login"
   // val reply = restCaller.get(eplBaseHost+urlExtension, readToken)
   // val result = jsonMapper.readValue(reply, classOf[token])
   // writeToken(Some(result))
  //  result
    Token.empty()
  }



  def getMetrics() = {
    val urlExtension= "/api/ESS/GetLastPowerDataBySN"
    val reply = restCaller.simpleRestPostCall(eplBaseHost+urlExtension,  new GetMetricsDetails("ALL", true),true,token.AccessToken)

    val metrics = (jsonMapper.readValue(reply, classOf[SystemDetailsReply]).data)
    logger.info("AlphaEss Metrics Completed")
    currentBatteryPercentage = metrics.pbat
    currentGridPull = metrics.pmeter_l1
    reporter.write(metrics)
  }


  def resetDailyCounter() =
  {
    reporter.DailySolarGeneration = 0
  }

  def getSystemSettings(): AlphaESSReceivedSettingData =
  {
    val urlExtension= "/api/Account/GetCustomUseESSSetting"//?system_id="+sys_sn
    //val getParameters = new util.ArrayList[NameValuePair](2)
    //getParameters.add(new BasicNameValuePair("sys_sn", sys_sn))
    val reply = restCaller.simpleRestGetCall(eplBaseHost+urlExtension,
      withToken = true,
      token = token.AccessToken)

    jsonMapper.readValue(reply, classOf[AlphaESSReceivedSetting]).data

  }

  def setSystemSettings(convertedPayload: AlphaESSSendSetting)
  {
    val urlExtension= "/api/Account/CustomUseESSSetting"
    //convert to AlphaESSSendSetting - what they give and what they expect are not the same :(

    val reply = restCaller.simpleRestPostCall(eplBaseHost+urlExtension,convertedPayload,true,token.AccessToken)
    val result = jsonMapper.readValue(reply, classOf[LoginReply])

    if(result.code == 200)
      logger.info("Updated System Settings")
  }

  def getBatteryPercentage(): Double = currentBatteryPercentage

  def getCurrentGridPull():Double = {
    currentGridPull
  }
}
