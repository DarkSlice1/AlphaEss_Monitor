package api.forecast.solar

import api.alpha.AlphaObjectMapper.AlphaESSSendSetting
import api.alpha.alpha
import com.typesafe.scalalogging.LazyLogging
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
    areWeInTheChargingWindow(Calendar.getInstance())
    if(batteryChargeEnabled && CurrentGridPull > 0.0 && CurrentGridPull < 1000.0) { //are we pulling a little bit from the grid
      if (areWeInTheChargingWindow(Calendar.getInstance())) {
        //stop using the grid for power - switch to the battery
        alpha.setSystemSettings(AlphaESSSendSetting.from(alpha.getSystemSettings()).copy(grid_charge = 0))
        batteryChargeEnabled = false
        logger.info("Battery charging Disabled")
      }
    }
  }

  def EnableBatteryNightCharging()={
    if(!batteryChargeEnabled) {
      alpha.setSystemSettings(AlphaESSSendSetting.from(alpha.getSystemSettings()).copy(grid_charge = 1))
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
    val ChargingWindowStart = Calendar.getInstance()
    ChargingWindowStart.set(Calendar.HOUR_OF_DAY,2)
    ChargingWindowStart.set(Calendar.MINUTE,5)
    ChargingWindowStart.set(Calendar.SECOND,0)

    val ChargingWindowEnd = Calendar.getInstance
    ChargingWindowEnd.set(Calendar.HOUR_OF_DAY,4)
    ChargingWindowEnd.set(Calendar.MINUTE,55)
    ChargingWindowEnd.set(Calendar.SECOND,0)

    (now.getTime.after(ChargingWindowStart.getTime) && now.getTime.before(ChargingWindowEnd.getTime))
  }
}
