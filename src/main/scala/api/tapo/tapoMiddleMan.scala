package api.tapo

import com.typesafe.config.{Config, ConfigFactory}
import metrics.KamonMetrics

import java.util
import java.util.{Arrays, HashMap, List, Map}
import scala.collection.JavaConversions.mapAsScalaMap
import scala.reflect.internal.util.Statistics.canEnable.||


class tapoMiddleMan(tapoParamenter: Tapo) {

  val reporterKamon = new KamonMetrics()
  val tapo = tapoParamenter

  val aquarium_tapoEnergyUsage = reporterKamon.tapoEnergyUsageGauge.add().withTag("sys_name","")

  def Run(): Unit = {

    val config = ConfigFactory.load
    val username = config.getString("tapo.username")
    val password = config.getString("tapo.password")
    val addresses: Array[String] = config.getString("tapo.addresses").split(",")

    var token = new util.HashMap[String, String]
    addresses foreach { case (address) =>

      if (tapo.token.getOrElse(address, "") == "") {
        tapo.Setup(username, password, address)
      }
    }



    //TODO Error handling

    addresses foreach { case (address) => {
      try {
        val energyUsage = tapo.Run(address)
        reporterKamon.tapoEnergyUsageCounter.increment((energyUsage / 100).toLong, "ipAddress", address)
        if (energyUsage == 0) {
          reporterKamon.tapoEnergyUsageGauge.set(0, "ipAddress", address)
        }
        else {
          reporterKamon.tapoEnergyUsageGauge.set((energyUsage / 100).toLong, "ipAddress", address)
        }
      }
      catch {
        case ex: Exception =>
          println("ERROR Tapo : "+address+" - clearing its token : " + ex.toString)
          tapo.token.put(address,"")
      }
    }
    }
  }
}
