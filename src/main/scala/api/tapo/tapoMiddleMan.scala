package api.tapo

import com.typesafe.config.Config
import metrics.KamonMetrics

import scala.collection.JavaConversions.mapAsScalaMap



class tapoMiddleMan(tapoParamenter: Tapo, config :Config) {

  val reporterKamon = new KamonMetrics()
  val tapo = tapoParamenter

  def Run(): Unit = {
    val username = config.getString("tapo.username")
    val password = config.getString("tapo.password")
    val addresses: Array[String] = config.getString("tapo.addresses").split(",")

    addresses foreach { case (address) =>
      try {
        if (tapo.token.getOrElse(address, "") == "") {
          tapo.Setup(username, password, address)
        }
      }
      catch {
        case ex: Exception =>
          println("Tapo Energy: " + address + " = " + ex.getMessage)
          //reset the device to start over
          tapo.token.remove(address)
          tapo.c658a.remove(address)
          tapo.handshakeResponse.remove(address)
      }
    }

    addresses foreach { case (address) => {
      try {
        if (tapo.token.getOrElse(address, "") != "") {
          val energyUsage = tapo.Run(address)
          reporterKamon.tapoEnergyUsageCounter.increment((energyUsage / 100).toLong, "ipAddress", address)
          if (energyUsage == 0) {
            reporterKamon.tapoEnergyUsageGauge.set(0, "ipAddress", address)
          }
          else {
            reporterKamon.tapoEnergyUsageGauge.set((energyUsage / 100).toLong, "ipAddress", address)
          }
        }
      }
      catch {
        case ex: Exception =>
          println("Tapo Energy: "+address+" = " + ex.getMessage)
          tapo.token.remove(address)
          tapo.c658a.remove(address)
          tapo.handshakeResponse.remove(address)
      }
    }
    }
  }
}
