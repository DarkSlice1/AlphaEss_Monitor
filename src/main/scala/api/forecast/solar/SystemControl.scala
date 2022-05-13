package api.forecast.solar

import api.alpha.AlphaObjectMapper.AlphaESSSendSetting
import api.alpha.alpha
import com.typesafe.scalalogging.LazyLogging
import java.text.SimpleDateFormat
import java.util.Calendar

class SystemControl(alpha: alpha,forecast:SolarForecast) extends LazyLogging {

  private var batteryChargeEnabled = true

  def setSystemSettingsBasedOnGeneratedForecast(): Unit ={
    val todaysForecast = forecast.getTodaysForcast()
    forecast.publishTodaysForcast(todaysForecast)
    logger.info("Publish forecasting Metrics")

    todaysForecast match
    {
      case x if x>15000 => alpha.setSystemSettings(SetBatteryToX(30))
      case x if(x<15000 && x>10000) => alpha.setSystemSettings(SetBatteryToX(50))
      case x if(x<10000 && x>6000) => alpha.setSystemSettings(SetBatteryToX(80))
      case x if x<6000 => alpha.setSystemSettings(SetBatteryToX(95))
      case _ =>  alpha.setSystemSettings(SetBatteryToX(95))
    }
  }

  def canWeTurnOffNightCharging(CurrentGridPull:Double)={
    logger.info("Battery Control charge value ="+CurrentGridPull)
    areWeInTheChargingWindow(Calendar.getInstance()))
    if(batteryChargeEnabled && CurrentGridPull > 0.0 && CurrentGridPull < 1000.0) { //are we pulling a little bit from the grid
      if (areWeInTheChargingWindow(Calendar.getInstance())) {
        //stop using the grid for power - switch to the battery
        //alpha.setSystemSettings(AlphaESSSendSetting.from(alpha.getSystemSettings()).copy(grid_charge = 0))
        batteryChargeEnabled = false
        logger.info("Battery charging Disabled")
      }
    }
  }

  def EnableBatteryNightCharging()={
    if(!batteryChargeEnabled) {
      //alpha.setSystemSettings(AlphaESSSendSetting.from(alpha.getSystemSettings()).copy(grid_charge = 1))
      batteryChargeEnabled = true
      logger.info("Battery charging Enabled")
    }
  }

  def SetBatteryToX(batteryPercentage : Int): AlphaESSSendSetting =
  {
    val newBatterySettings  =  AlphaESSSendSetting.from(alpha.getSystemSettings()).copy(bat_high_cap=""+batteryPercentage)
    logger.info("Battery percent will be: "+batteryPercentage+"%")
    newBatterySettings
  }

  def areWeInTheChargingWindow(now :Calendar): Boolean = {
    val ChargingWindowStart = Calendar.getInstance
    ChargingWindowStart.setTime(new SimpleDateFormat("HH:mm:ss").parse("02:05:00"))
    ChargingWindowStart.add(Calendar.DATE, 1)

    val ChargingWindowEnd = Calendar.getInstance
    ChargingWindowEnd.setTime(new SimpleDateFormat("HH:mm:ss").parse("05:55:00"))
    ChargingWindowEnd.add(Calendar.DATE, 1)

    logger.info("Are we after 02:05"+now.getTime.after(ChargingWindowStart.getTime))
    logger.info("Are we before 05:55"+now.getTime.before(ChargingWindowEnd.getTime))

    (now.getTime.after(ChargingWindowStart.getTime) && now.getTime.before(ChargingWindowEnd.getTime))
  }
}
