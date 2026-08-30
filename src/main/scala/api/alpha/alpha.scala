package api.alpha

import api.alpha.AlphaObjectMapper._
import api.common.FileIO._
import api.common.Token
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import metrics.KamonMetrics
import org.apache.http.NameValuePair

import java.time.Instant
import java.util
import java.util.Date

class alpha(config: Config, reporterKamon : KamonMetrics) extends LazyLogging{

  val username = config.getString("alphaess.username")
  val password = config.getString("alphaess.password")
      var systemId = ""

  val reporter = new reportHome(config,reporterKamon)
  val eplBaseHost = "https://platform-eur.alphaess.com"

  var token = Token.empty()
  var currentBatteryPercentage = 0.0
  var currentGridPull = 0.0

  def run(): Unit = {
    //Do we have an access token
    token match {

      // No - empty token object returned
      case token if (token.token == "") =>
        Login(); run;

      // Yes
      case token if (token.token != "")=>
          getMetrics()
    }
  }

  def Login():LoginReply ={
    val urlExtension= "/api/users-center/sessions"
    val reply = restCaller.simpleRestPostCall(eplBaseHost+urlExtension, "{\"email\":\""+username+"\", \"type\":\"password\",\"password\":\""+password+"\"}")
    val result: LoginReply = jsonMapper.readValue(reply, classOf[LoginReply])

    result match {
      case null => logger.error("ERROR : " + result.toString)
      case value : Any =>  token = new Token(value.accessToken,value.expiresIn,Date.from(Instant.now()).toString,value.refreshToken)
    }
    result
  }

  def getMetrics(): Unit = {

    try {
      val urlExtension = "/api/internal/v1/sites/qt2RsJ8DRZEdUMa44e/real-status"

      val postParameters = new util.ArrayList[NameValuePair](2);
      val reply = restCaller.simpleRestGetCall(eplBaseHost + urlExtension, true, postParameters, true, token.token)
      val metrics = (jsonMapper.readValue(reply, classOf[SystemDetailsReply]))
      logger.info("AlphaEss Metrics Completed")
      systemId = metrics.batteryHeatingList.head.sysSn
      currentBatteryPercentage = metrics.power.soc
      currentGridPull = metrics.power.grid
      reporter.write(metrics.power)
    }
   catch  {
     case ex:Exception =>
       logger.error(ex.toString+" Resetting Token")
       token = Token.empty()

   }
  }

  def resetDailyCounter(): Unit =
  {
    reporter.DailySolarGeneration = 0
  }

  def getSystemSettings: AlphaESSCycleStrategy =
  {
    val urlExtension= "/api/internal/v1/sites/qt2RsJ8DRZEdUMa44e/setting" //? what is this value...
    val reply = restCaller.simpleRestGetCall(eplBaseHost+urlExtension,
      withToken = true,
      token = token.token)

    jsonMapper.readValue(reply, classOf[AlphaESSCycleStrategy])
  }

  def setSystemSettings(convertedPayload: AlphaESSCycleStrategy): Unit = {
    val urlExtension= "/api/internal/v1/sites/qt2RsJ8DRZEdUMa44e/setting"
    val reply = restCaller.simpleRestPutCall(eplBaseHost+urlExtension, jsonMapper.writeValueAsString(convertedPayload),true,token.token)
    if(reply != "")
     jsonMapper.readValue(reply, classOf[LoginReply])
  }

  def getFeedStrategyList: AlphaESSFeedStrategyList = {
    //TODO remove ID in URL
    val urlExtension= "/api/internal/v1/ess/"+systemId+"?components=feedInControl" //? what is this value...
    val reply = restCaller.simpleRestGetCall(eplBaseHost+urlExtension,
      withToken = true,
      token = token.token)

    jsonMapper.readValue(reply, classOf[AlphaESSFeedStrategyList])
  }

  def setFeedStrategy(config : AlphaESSFeedStrategyList): Unit = {
    //TODO remove ID in URL
    val urlExtension= "/api/internal/v1/ess/"+systemId
    val jsonString: String = jsonMapper.writeValueAsString(config)
    val reply = restCaller.simpleRestPutCall(eplBaseHost+urlExtension, jsonString ,true,token.token)
    if(reply != "")
      jsonMapper.readValue(reply, classOf[UpdateFITReply])

    logger.info("Updated FIT System Settings")
  }

  def getBatteryPercentage: Double = currentBatteryPercentage

  def getCurrentGridPull:Double = {
    currentGridPull
  }
}
