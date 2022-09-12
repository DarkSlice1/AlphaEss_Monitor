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
     //multiplying by 10 to align with other metrics
    try{
      //reporterKamon.zappiEnergyUsageCounter.increment((conversion.eddi.head.ectp1*10).toLong, "hub", username)
      if (conversion.eddi.head.div == 0) {
        //reporterKamon.eddiEnergyUsageGauge.set(0, "hub", username)
        reporterKamon.eddiEnergyTemperature1.set(0, "hub", username)
        reporterKamon.eddiEnergyTemperature2.set(0, "hub", username)

      }
      else {
        //reporterKamon.eddiEnergyUsageGauge.set((conversion.eddi.head.ectp1 * 10).toLong, "hub", username)
        reporterKamon.eddiEnergyTemperature1.set(conversion.eddi.head.tp1, "hub", username)
        reporterKamon.eddiEnergyTemperature2.set(conversion.eddi.head.tp2, "hub", username)

      }
      if (serial == 0) {
        serial = conversion.eddi.head.sno
        logger.info("Eddi serial captured "+serial)
      }
      logger.info("Eddi Metrics Completed")

    }
    catch {
      case ex: Exception => logger.error("ERROR: " + ex.toString);
    }
  }
}