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
      val trueAddress = ExtractAddress(address)

      try {
        if (tapo.token.getOrElse(trueAddress, "") == "") {
          tapo.Setup(username, password, trueAddress)
        }
      }
      catch {
        case ex: Exception =>
          logger.info("Tapo Energy: " + trueAddress + " = " + ex.getMessage)
          //reset the device to start over
          tapo.token.remove(trueAddress)
          tapo.c658a.remove(trueAddress)
          tapo.handshakeResponse.remove(trueAddress)
      }
    }

    addresses foreach { case (address) => {
      val trueAddress = ExtractAddress(address)
      val trueTag = ExtractTag(address)

      try {
        if (tapo.token.getOrElse(trueAddress, "") != "") {
          val energyUsage = tapo.Run(trueAddress,trueTag)
          reporterKamon.tapoEnergyUsageCounter.increment((energyUsage / 100).toLong, "ipAddress", address, "device",trueTag)
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
          logger.info("Tapo Energy: "+address+" = " + ex.getMessage)
          tapo.token.remove(address)
          tapo.c658a.remove(address)
          tapo.handshakeResponse.remove(address)
      }
    }
    }
  }

  def ExtractAddress(address:String): String={
    var trueAddress = ""
    if(address.contains(":")) { //encase we want to add our own tags to the IP address
      trueAddress=address.split(":").head
    }
    else
      trueAddress=address

    trueAddress
  }

  def ExtractTag(address:String): String={
    var trueAddress = ""
    if(address.contains(":")) { //encase we want to add our own tags to the IP address
      trueAddress=address.split(":").last
    }
    else
      trueAddress=address

    trueAddress
  }

}
