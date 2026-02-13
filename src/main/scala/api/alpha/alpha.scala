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
  var systemId = ""

  val reporter = new reportHome(config,reporterKamon)
  val eplBaseHost = "https://cloud.alphaess.com"

  var token = Token.empty()
  var currentBatteryPercentage = 0.0
  var currentGridPull = 0.0

  def run(): Unit = {
    //Do we have an access token
    token match {

      // No - empty token object returned
      case token if (token.token == "") =>
        Login(); getSystemID(); run;

      // Yes
      case token if (token.token != "")=>
          getMetrics()
    }
  }

  def Login():LoginReply ={
    val urlExtension= "/api/stable/user/login"
    val reply = restCaller.simpleRestPostCall(eplBaseHost+urlExtension, "{\"username\":\""+username+"\",\"password\":\""+password+"\"}")
    val result: LoginReply = jsonMapper.readValue(reply, classOf[LoginReply])

    result.data match {
      case null => logger.error("ERROR : " + result.toString)
      case value : Any =>  token = value
    }
    result
  }

  def refreshToken(): Token = {
    Token.empty()
  }



  def getMetrics(): Unit = {
    val urlExtension= "/api/report/energyStorage/getLastPowerData"

    val postParameters = new util.ArrayList[NameValuePair](2);
    postParameters.add(new BasicNameValuePair("sysSn", sys_sn));
    val reply = restCaller.simpleRestGetCall(eplBaseHost+urlExtension,true, postParameters, true, token.token)
    val metrics = (jsonMapper.readValue(reply, classOf[SystemDetailsReply]).data)
    logger.info("AlphaEss Metrics Completed")
    currentBatteryPercentage = metrics.soc
    currentGridPull = metrics.pgrid
    reporter.write(metrics)
  }


  def resetDailyCounter(): Unit =
  {
    reporter.DailySolarGeneration = 0
  }

  def getSystemID(): Unit =
  {
    val urlExtension= "/api/stable/home/getCustomMenuEssList"
    val reply = restCaller.simpleRestGetCall(eplBaseHost+urlExtension,
      withToken = true,
      token = token.token)

    val array = jsonMapper.readValue(reply, classOf[AlphaESSGetCustomMenuEssList]).data

    array.foreach{x =>
      if(x.sysSn == sys_sn) {
        systemId = x.systemId
      }
    }
  }

  def getBatteryPercentage: Double = currentBatteryPercentage

  def getCurrentGridPull:Double = {
    currentGridPull
  }

  def getSystemSettingsV2: CycleData =
  {
    val urlExtension= "/api/iterate/sysSet/getCycleStrategy?id="+systemId //? what is this value...
    val reply = restCaller.simpleRestGetCall(eplBaseHost+urlExtension,
      withToken = true,
      token = token.token)

    jsonMapper.readValue(reply, classOf[AlphaESSCycleStrategy]).data
  }

  def setSystemSettingsV2(convertedPayload: CycleDataUpdate): Unit = {
    val urlExtension= "/api/iterate/sysSet/setCycleStrategy"
    val reply = restCaller.simpleRestPutCall(eplBaseHost+urlExtension, jsonMapper.writeValueAsString(convertedPayload),true,token.token)
    val result = jsonMapper.readValue(reply, classOf[LoginReply])

    if(result.code == 200)
      logger.info("Updated System Settings")
  }



  //for alpha gen2
  def getFeedStrategyList: AlphaEssFeedStrategyData = {

    val urlExtension= "/api/iterate/sysSet/getFeedStrategyList?id="+systemId //? what is this value...
    val reply = restCaller.simpleRestGetCall(eplBaseHost+urlExtension,
      withToken = true,
      token = token.token)

    jsonMapper.readValue(reply, classOf[AlphaESSFeedStrategyList]).data
  }

  //for alpha gen2
  def setFeedStrategy(config : AlphaEssFeedStrategyConfig): Unit = {

    val urlExtension= "/api/iterate/sysSet/saveFeedStrategy"
    //val reply = restCaller.simpleRestPostCall(eplBaseHost+urlExtension, "{\"username\":\""+username+"\",\"password\":\""+password+"\"}")

    val jsonString: String = jsonMapper.writeValueAsString(config)
    val reply = restCaller.simpleRestPostCall(eplBaseHost+urlExtension, jsonString ,true,token.token)
    val result = jsonMapper.readValue(reply, classOf[UpdateFITReply])

    if(result.code == 200)
      logger.info("Updated FIT System Settings")
  }
}
