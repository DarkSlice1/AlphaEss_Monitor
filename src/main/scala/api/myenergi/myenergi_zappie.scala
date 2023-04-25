package api.myenergi

import api.common.FileIO._
import api.myenergi.MyEnergiObjectMapper._
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import metrics.KamonMetrics

//available endpoints : https://github.com/claytonn73/myenergi_api/blob/62d6915784bde9aaaa6fbfe34cc0ec6eeb2eb060/myenergi/const.py#L9

class myenergi_zappie(config: Config, reporterKamon : KamonMetrics) extends LazyLogging{


  val username = config.getString("myenergi.username")
  val password = config.getString("myenergi.password")
  val myenergi_BaseHost = "https://director.myenergi.net"
  var asn_url = "s18.myenergi.net"
  var serial = 0

  def Run(): Unit = {

    try {
       getMetrics()
    }
    catch {
      case ex: Exception => logger.error("Zappie ERROR: " + ex.toString + " Trying to login again")
        //reset
        asn_url = "s18.myenergi.net"
    }
  }

//api broken (now just returns "hello world"), hard coding url to be "s18.myenergi.net"
  def Login() = {
    val urlExtension = ""
    val reply = restCaller.simpleRestGetCallDigest(
      url = myenergi_BaseHost + urlExtension,
      username = username,
      password = password,
      host = myenergi_BaseHost.replace("https://",""),
      digestUri = urlExtension
    )
    asn_url  = jsonMapper.readValue(reply, classOf[DigestReplyDetails]).asn
  }

  def getMetrics() = {
    val urlExtension = "/cgi-jstatus-Z"
    val reply = restCaller.simpleRestGetCallDigest(
      url = "https://"+asn_url + urlExtension,
      username = username,
      password = password,
      host = asn_url,
      digestUri = urlExtension
    )
   val conversion = jsonMapper.readValue(reply, classOf[jstatusZReply])
     //multiplying by 10 to align with other metrics
    try{
      reporterKamon.zappiEnergyUsageCounter.increment((conversion.zappi.head.ectp1*10).toLong, "hub", username)
      reporterKamon.zappiVoltageGauge.set(conversion.zappi.head.vol, "hub", username)
      reporterKamon.zappiVoltageFrequencyGauge.set((conversion.zappi.head.frq).toInt, "hub", username)

      if (conversion.zappi.head.div == 0) {
        reporterKamon.zappiEnergyUsageGauge.set(0, "hub", username)
      }
      else {
        reporterKamon.zappiEnergyUsageGauge.set((conversion.zappi.head.ectp1 * 10).toLong, "hub", username)
      }

      if (serial == 0) {
        serial = conversion.zappi.head.sno
        logger.info("Zappi serial captured "+serial)
      }
      logger.info("Zappi Metrics Completed")

    }
    catch {
      case ex: Exception => logger.error("ERROR: " + ex.toString);
    }
  }

  def DoNightBoost(Quantity:Int, EndTime : String): Unit =
  {
    if(serial != 0)
      {
        //set to EcoPlus first
        SetEcoPlusMode()
        try {
          val urlExtension = "/cgi-zappi-mode-Z" + serial + "-0-10-" + Quantity + "-" + EndTime
          restCaller.simpleRestGetCallDigest(
            url = "https://" + asn_url + urlExtension,
            username = username,
            password = password,
            host = asn_url,
            digestUri = urlExtension
          )
          logger.info("Zappi boost set for "+Quantity+"Kw and due to end at "+EndTime)
        }
        catch {
          case ex: Exception => logger.error("ERROR: " + ex.toString);
        }
      }
  }

  def SetEcoPlusMode(): Unit =
  {
    try {
      val urlExtension = "/cgi-zappi-mode-Z" + serial + "-1-0-0-0000"
      restCaller.simpleRestGetCallDigest(
        url = "https://" + asn_url + urlExtension,
        username = username,
        password = password,
        host = asn_url,
        digestUri = urlExtension
      )
      logger.info("Zappi Set to Fast Charge Mode")
    }
    catch {
      case ex: Exception => logger.error("ERROR: " + ex.toString);
    }
  }

  def SetStopMode(): Unit =
  {
    try {
      val urlExtension = "/cgi-zappi-mode-Z" + serial + "-4-0-0-0000"
      restCaller.simpleRestGetCallDigest(
        url = "https://" + asn_url + urlExtension,
        username = username,
        password = password,
        host = asn_url,
        digestUri = urlExtension
      )
      logger.info("Zappi Set to Stop Mode")
    }
    catch {
      case ex: Exception => logger.error("ERROR: " + ex.toString);
    }
  }
}