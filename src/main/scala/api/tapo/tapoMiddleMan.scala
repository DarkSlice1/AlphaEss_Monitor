package api.tapo

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import metrics.KamonMetrics

import scala.collection.JavaConversions.mapAsScalaMap



class tapoMiddleMan(tapoParamenter: Tapo, config :Config, reporterKamon : KamonMetrics) extends LazyLogging{

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
          logger.info("Tapo Energy: " + address + " = " + ex.getMessage)
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

          address match {
            case "192.168.1.30" =>
              if (energyUsage == 0) {
                reporterKamon.pieEnergyUsageGauge.add((0),"pie","Aquarium")
              }
              else {
                reporterKamon.pieEnergyUsageGauge.add((energyUsage / 1000),"pie","Aquarium")
              }
            case "192.168.1.39" =>
              if (energyUsage == 0) {
                reporterKamon.pieEnergyUsageGauge.add((0),"pie","Office")
              }
              else {
                reporterKamon.pieEnergyUsageGauge.add((energyUsage / 1000),"pie","Office")
              }

            case "192.168.1.56" =>
              if (energyUsage == 0) {
                reporterKamon.pieEnergyUsageGauge.add((0),"pie","Dishwasher")
              }
              else {
                reporterKamon.pieEnergyUsageGauge.add((energyUsage / 1000),"pie","Dishwasher")
              }

            case "192.168.1.37" =>
              if (energyUsage == 0) {
                reporterKamon.pieEnergyUsageGauge.add((0),"pie","Washer_And_Dryer")
              }
              else {
                reporterKamon.pieEnergyUsageGauge.add((energyUsage / 1000),"pie","Washer_And_Dryer")
              }

            case "192.168.1.47" =>
              if (energyUsage == 0) {
                reporterKamon.pieEnergyUsageGauge.add((0),"pie","TV")
              }
              else {
                reporterKamon.pieEnergyUsageGauge.add((energyUsage / 1000),"pie","TV")
              }

            case "192.168.1.48" =>
              if (energyUsage == 0) {
                reporterKamon.pieEnergyUsageGauge.add((0),"pie","Fridge")
              }
              else {
                reporterKamon.pieEnergyUsageGauge.add((energyUsage / 1000),"pie","Fridge")
              }

            case "192.168.1.35" =>
              if (energyUsage == 0) {
                reporterKamon.pieEnergyUsageGauge.add((0),"pie","Modem_And_Sitting_Room_Lights")
              }
              else {
                reporterKamon.pieEnergyUsageGauge.add((energyUsage / 1000),"pie","Modem_And_Sitting_Room_Lights")
              }

            case "192.168.1.197" =>
              if (energyUsage == 0) {
                reporterKamon.pieEnergyUsageGauge.add((0),"pie","Hoover")
              }
              else {
                reporterKamon.pieEnergyUsageGauge.add((energyUsage / 1000),"pie","Hoover")
              }

            case _ =>
          }
        }
      }
      catch {
        case ex: Exception =>
          logger.info("Tapo Energy: "+address+" = " + ex.getMessage)
          tapo.token.remove(address)
          tapo.c658a.remove(address)
          tapo.handshakeResponse.remove(address)
      }
    }
    }
  }
}
