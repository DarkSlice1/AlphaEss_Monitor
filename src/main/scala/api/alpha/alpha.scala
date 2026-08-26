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
  val sys_sn = config.getString("alphaess.system_sn")
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

  def refreshToken(): Token = {
    Token.empty()
  }

  def getMetrics(): Unit = {

    try {
      val urlExtension = "/api/internal/v1/sites/qt2RsJ8DRZEdUMa44e/real-status"

      val postParameters = new util.ArrayList[NameValuePair](2);
      val reply = restCaller.simpleRestGetCall(eplBaseHost + urlExtension, true, postParameters, true, token.token)
      val metrics = (jsonMapper.readValue(reply, classOf[SystemDetailsReply]))
      logger.info("AlphaEss Metrics Completed")
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

  //for alpha gen2
  def setFeedStrategy(config : AlphaESSCycleStrategy): Unit = {

    val urlExtension= "/api/internal/v1/sites/qt2RsJ8DRZEdUMa44e/setting"
    val jsonString: String = jsonMapper.writeValueAsString(config)
    val reply = restCaller.simpleRestPutCall(eplBaseHost+urlExtension, jsonString ,true,token.token)
    val result = jsonMapper.readValue(reply, classOf[UpdateFITReply])

    if(result.code == 200)
      logger.info("Updated FIT System Settings")
  }



  //for alpha gen2
  def getFeedStrategyList: AlphaEssFeedStrategyData = {

    val urlExtension= "/api/iterate/sysSet/getFeedStrategyList?id="+systemId //? what is this value...
    val reply = restCaller.simpleRestGetCall(eplBaseHost+urlExtension,
      withToken = true,
      token = token.token)

    jsonMapper.readValue(reply, classOf[AlphaESSFeedStrategyList]).data
  }

  def getBatteryPercentage: Double = currentBatteryPercentage

  def getCurrentGridPull:Double = {
    currentGridPull
  }
}
