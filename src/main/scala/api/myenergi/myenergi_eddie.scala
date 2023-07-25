package api.myenergi

import api.common.FileIO._
import api.myenergi.MyEnergiObjectMapper._
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import metrics.KamonMetrics

//available endpoints : https://github.com/claytonn73/myenergi_api/blob/62d6915784bde9aaaa6fbfe34cc0ec6eeb2eb060/myenergi/const.py#L9

class myenergi_eddie(config: Config, reporterKamon : KamonMetrics) extends LazyLogging{


  val username = config.getString("myenergi.username")
  val password = config.getString("myenergi.password")
  val myenergi_BaseHost = "https://director.myenergi.net"
  var asn_url = "s18.myenergi.net"
  var serial = 24459385

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

//api broken (now just returns "hello world""), hard coding url to be "s18.myenergi.net"
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
    val urlExtension = "/cgi-jstatus-E"
    val reply = restCaller.simpleRestGetCallDigest(
      url = "https://"+asn_url + urlExtension,
      username = username,
      password = password,
      host = asn_url,
      digestUri = urlExtension
    )
   val conversion = jsonMapper.readValue(reply, classOf[jstatusEReply])
    try {
      if (conversion.eddi.head.hno == 1) //which tank is being heated
      {
        reporterKamon.eddiEnergyUsageCounter.increment((conversion.eddi.head.div * 10).toLong, "Tank1", username)
      }
      else {
        reporterKamon.eddiEnergyUsageCounter.increment((conversion.eddi.head.div * 10).toLong, "Tank2", username)
      }

      reporterKamon.eddiEnergyTemperature1.set(conversion.eddi.head.tp1, "hub", username)
      reporterKamon.eddiEnergyTemperature2.set(conversion.eddi.head.tp2, "hub", username)

      if (conversion.eddi.head.div == 0) { //clear values if no longer drawing energy
        reporterKamon.eddiEnergyUsageGauge.set(0, "Tank1", username)
        reporterKamon.eddiEnergyUsageGauge.set(0, "Tank2", username)
      }
      else {
        if (conversion.eddi.head.hno == 1) //which tank is being heated
        {
          reporterKamon.eddiEnergyUsageGauge.set((conversion.eddi.head.div * 10).toLong, "Tank1", username)
          reporterKamon.eddiEnergyUsageGauge.set(0, "Tank1", username)
        }
        else {
          reporterKamon.eddiEnergyUsageGauge.set((conversion.eddi.head.div * 10).toLong, "Tank2", username)
          reporterKamon.eddiEnergyUsageGauge.set(0, "Tank2", username)
        }

      }
      if (serial == 0) {
        serial = conversion.eddi.head.sno
        logger.info("Eddi serial captured " + serial)
      }
      logger.info("Eddi Metrics Completed")

    }
    catch {
      case ex: Exception => logger.error("ERROR: " + ex.toString);
    }
  }

  def SetNormalMode(): Unit =
  {
    try {
      val urlExtension = "/cgi-eddi-mode-E" + serial + "-1"
      restCaller.simpleRestGetCallDigest(
        url = "https://" + asn_url + urlExtension,
        username = username,
        password = password,
        host = asn_url,
        digestUri = urlExtension
      )
      logger.info("Eddie Set to Normal Mode")
    }
    catch {
      case ex: Exception => logger.error("ERROR: " + ex.toString);
    }
  }

  def SetStopMode(): Unit =
  {
    try {
      val urlExtension = "/cgi-eddi-mode-E" + serial + "-0"
      restCaller.simpleRestGetCallDigest(
        url = "https://" + asn_url + urlExtension,
        username = username,
        password = password,
        host = asn_url,
        digestUri = urlExtension
      )
      logger.info("Eddie Set to Stop Mode")
    }
    catch {
      case ex: Exception => logger.error("ERROR: " + ex.toString);
    }
  }
}