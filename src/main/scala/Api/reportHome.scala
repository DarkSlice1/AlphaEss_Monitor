package Api

import Api.objectMapper.getHomeById
import metrics.KamonMetrics

class reportHome {

  val reporterKamon = new KamonMetrics()

  def write(home : getHomeById): Unit = {

    home.data.receivers.map(receiver => {
      receiver.zones.map(zone =>{
        //cant send double via kamon, so multiple by 10 and divide on the other side
        val currentTemp =(zone.currentTemperature*10).toLong
        //cant send double via kamon, so multiple by 10 and divide on the other side
        val targetTemp = (zone.targetTemperature*10).toLong
        val name = zone.name
        //true = 1, false = 0
        val burn = if(zone.isBoostActive) 1 else 0
        //true = 1, false = 0
        val online = if(zone.isOnline) 1 else 0

        reporterKamon.CurrentTemp.record(currentTemp,Map(
          "name"->name
        ))

        reporterKamon.TargetTemp.record(targetTemp,Map(
          "name"->name
        ))

        reporterKamon.BurnActive.record(burn,Map(
          "name"->name
        ))

        reporterKamon.IsOnline.record(online,Map(
          "name"->name
        ))

      })
    })

  }
}
