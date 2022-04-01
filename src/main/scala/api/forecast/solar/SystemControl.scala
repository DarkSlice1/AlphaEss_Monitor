package api.forecast.solar

import api.alpha.AlphaObjectMapper.AlphaESSSendSetting
import api.alpha.alpha
import com.typesafe.scalalogging.LazyLogging
import java.time.Instant
import java.text.SimpleDateFormat
import java.util.Calendar

class SystemControl(alpha: alpha,forecast:SolarForecast) extends LazyLogging {

  private var batteryChargeEnabled = true
  val ChargingWindowStart = Calendar.getInstance
  ChargingWindowStart.setTime(new SimpleDateFormat("HH:mm:ss").parse("02:05:00"))
  ChargingWindowStart.add(Calendar.DATE, 1)

  val ChargingWindowEnd = Calendar.getInstance
  ChargingWindowEnd.setTime(new SimpleDateFormat("HH:mm:ss").parse("05:55:00"))
  ChargingWindowEnd.add(Calendar.DATE, 1)

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
    if(batteryChargeEnabled && CurrentGridPull < 1000.0) {
      if (Calendar.getInstance().getTime.after(ChargingWindowStart.getTime) &&  Calendar.getInstance().getTime.before(ChargingWindowEnd.getTime)) {
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
}
