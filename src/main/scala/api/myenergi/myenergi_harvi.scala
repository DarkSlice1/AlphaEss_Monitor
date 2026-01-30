package api.myenergi


import api.common.FileIO._
import api.myenergi.MyEnergiObjectMapper._
import com.fasterxml.jackson.core.`type`.TypeReference
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import metrics.KamonMetrics

//available endpoints : https://github.com/claytonn73/myenergi_api/blob/62d6915784bde9aaaa6fbfe34cc0ec6eeb2eb060/myenergi/const.py#L9

class myenergi_harvi(config: Config, reporterKamon : KamonMetrics) extends LazyLogging {

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
      host = myenergi_BaseHost.replace("https://", ""),
      digestUri = urlExtension
    )
    asn_url = jsonMapper.readValue(reply, classOf[DigestReplyDetails]).asn
  }

  def getMetrics() = {
    val urlExtension = "/cgi-jstatus-*"
    val reply = restCaller.simpleRestGetCallDigest(
      url = "https://" + asn_url + urlExtension,
      username = username,
      password = password,
      host = asn_url,
      digestUri = urlExtension
    )
    val conversion: List[MyEnergiEntry] = jsonMapper.readValue(reply, new TypeReference[List[MyEnergiEntry]]() {})

    try {
      conversion.view.flatMap(_.harvi.getOrElse(Nil)).foreach {
        //inverter
        //ct1 = solar
        //ct2 = garage
        //ct3 = heat pump
        case harvi if (harvi.sno == 14794285) =>
          reporterKamon.harviEnergyUsageCounter.increment(math.abs(harvi.ectp2).toLong, "garage", username)
          reporterKamon.harviEnergyUsageCounter.increment(math.abs(harvi.ectp3).toLong, "heatpump", username)
          reporterKamon.pieEnergyUsageGauge.add(math.abs(harvi.ectp2).toLong, "pie", "garage")
          reporterKamon.pieEnergyUsageGauge.add(math.abs(harvi.ectp3).toLong, "pie", "heat pump")

          if (harvi.ectp2 == 0) {reporterKamon.harviEnergyUsageGauge.set(0, "garage", username)}
          else {reporterKamon.harviEnergyUsageGauge.set(math.abs(harvi.ectp2).toLong, "garage", username)}

          if (harvi.ectp3 == 0) {reporterKamon.harviEnergyUsageGauge.set(0, "heatpump", username)}
          else {reporterKamon.harviEnergyUsageGauge.set(math.abs(harvi.ectp3).toLong, "heatpump", username)}

          logger.info("Harvi serial captured " + harvi.sno)

        //meter box
        //ct1 = Grid
        case harvi if (harvi.sno == 11608680) =>
          reporterKamon.pieEnergyUsageGauge.add(math.abs(harvi.ectp3).toLong, "pie", "mains2")
          logger.info("Harvi serial captured " + harvi.sno)

        //fusebox
        case harvi if (harvi.sno == 3162754) =>
          reporterKamon.pieEnergyUsageGauge.add(math.abs(harvi.ectp3).toLong, "pie", "mains")
          logger.info("Harvi serial captured " + harvi.sno)

        case _ =>
      }
      logger.info("Harvi Metrics Completed")
    }
    catch {
      case ex: Exception => logger.error("ERROR: " + ex.toString);
    }
  }
}