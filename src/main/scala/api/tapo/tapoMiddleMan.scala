package api.tapo

import metrics.KamonMetrics

import java.util
import scala.collection.JavaConversions.mapAsScalaMap


class tapoMiddleMan(tapoParamenter: Tapo) {

  val reporterKamon = new KamonMetrics()
  val tapo = tapoParamenter

  val aquarium_tapoEnergyUsage = reporterKamon.tapoEnergyUsageGauge.add().withTag("sys_name","")

  def Run(): Unit = {

    if (tapo.token.isEmpty) {
      tapo.Setup()
    }

    //TODO Error handling
    try {
      val metrics: util.Map[String, Integer] = tapo.Run()
      metrics foreach { case (key, value) => {
        reporterKamon.tapoEnergyUsageCounter.increment((value / 100).toLong, "ipAddress", key)
        reporterKamon.tapoEnergyUsageGauge.set((value / 100).toLong, "ipAddress", key)
      }
      }
    }
    catch {
      case ex: Exception => println("ERROR Running Tapo - Refreshing tokens : " + ex.toString);
        tapo.Setup()
    }
  }
}
