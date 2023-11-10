package api.forecast.solar


import api.alpha.AlphaObjectMapper.AlphaESSUpdateChargeConfigInfo
import api.alpha.alpha
import api.myenergi.{myenergi_eddie, myenergi_zappie}
import com.typesafe.scalalogging.LazyLogging

import java.util.Calendar

class SystemControl(alpha: alpha, zappi:myenergi_zappie, eddi:myenergi_eddie, forecast:SolarForecast) extends LazyLogging {

  private var batteryChargeEnabled = true
  private var batteryControlGridPullNoLongerNeededCounter = 0

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
    logger.info("Battery Control, Grid pull = "+CurrentGridPull)
    areWeInTheChargingWindow(Calendar.getInstance())
    if(batteryChargeEnabled && CurrentGridPull >= 0.0 && CurrentGridPull < 1000.0) { //are we pulling a little bit from the grid
      if (areWeInTheChargingWindow(Calendar.getInstance())) {
        //stop using the grid for power - switch to the battery
        if(batteryControlGridPullNoLongerNeededCounter > 18) { // 3 minutes
          alpha.setSystemSettings(AlphaESSUpdateChargeConfigInfo.from(alpha.getSystemSettings()).copy(gridCharge = 0))
          batteryChargeEnabled = false
          logger.info("Battery charging Disabled")
          zappi.SetStopMode()
          eddi.SetStopMode()
          batteryControlGridPullNoLongerNeededCounter = 0
        }
        else {
          logger.info("Battery Control - waiting for another iteration where grid pull is under 1kw/h ("+batteryControlGridPullNoLongerNeededCounter+"/18)")
          batteryControlGridPullNoLongerNeededCounter+=1
        }
      }
    }
  }

  def EnableBatteryNightCharging()={
    if(!batteryChargeEnabled) {
      alpha.setSystemSettings(AlphaESSUpdateChargeConfigInfo.from(alpha.getSystemSettings()).copy(gridCharge = 1))
      batteryChargeEnabled = true
      logger.info("Battery charging Enabled")
    }
  }

  def SetBatteryToX(batteryPercentage : Int): AlphaESSUpdateChargeConfigInfo =
  {
    val newBatterySettings  =  AlphaESSUpdateChargeConfigInfo.from(alpha.getSystemSettings()).copy(batHighCap=batteryPercentage)
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
