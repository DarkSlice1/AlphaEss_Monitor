package api.forecast.solar


import api.alpha.AlphaObjectMapper.AlphaESSUpdateChargeConfigInfo
import api.alpha.alpha
import api.myenergi.{myenergi_eddie, myenergi_zappie}
import com.typesafe.scalalogging.LazyLogging

import java.util.Calendar

class SystemControl(alpha: alpha, zappi:myenergi_zappie, eddi:myenergi_eddie, forecast:SolarForecast) extends LazyLogging {

  private var batteryChargeEnabled = false //default to false, we want to prioritize enabling in te event of an issue
  private var gridDumpEnabled = false
  private var batteryControlGridPullNoLongerNeededCounter = 0

  def setSystemSettingsBasedOnGeneratedForecast(): Unit ={
    val todaysForecast = forecast.getTodaysForcast()
    forecast.publishTodaysForcast(todaysForecast)
    logger.info("Publish forecasting Metrics")

    todaysForecast match
    {
      case x if x>15000 => alpha.setSystemSettings(SetBatteryToX(95))
      case x if(x<15000 && x>10000) => alpha.setSystemSettings(SetBatteryToX(95))
      case x if(x<10000 && x>6000) => alpha.setSystemSettings(SetBatteryToX(95))
      case x if x<6000 => alpha.setSystemSettings(SetBatteryToX(95))
      case _ =>  alpha.setSystemSettings(SetBatteryToX(95))
    }
  }

  def canWeTurnOffNightCharging(CurrentGridPull:Double)={
    logger.info("Battery Control, Grid pull = "+CurrentGridPull)
    areWeInTheChargingWindow(Calendar.getInstance())
    if(batteryChargeEnabled && CurrentGridPull > 0.0 && CurrentGridPull < 1000.0) { //are we pulling a little bit from the grid - if the value is 0, then we we are no longer get data from alpha- keep working as expected
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
      alpha.setSystemSettings(AlphaESSUpdateChargeConfigInfo.from(alpha.getSystemSettings()).copy(timeChaf2="00:00",timeChae2 = "00:00"))
      logger.info("Battery charging Period 2 reset")
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
    ChargingWindowStart.set(Calendar.HOUR_OF_DAY,2) //2:05am
    ChargingWindowStart.set(Calendar.MINUTE,5)
    ChargingWindowStart.set(Calendar.SECOND,0)

    val ChargingWindowEnd = Calendar.getInstance
    ChargingWindowEnd.set(Calendar.HOUR_OF_DAY,5) //5:55am
    ChargingWindowEnd.set(Calendar.MINUTE,55)
    ChargingWindowEnd.set(Calendar.SECOND,0)

    (now.getTime.after(ChargingWindowStart.getTime) && now.getTime.before(ChargingWindowEnd.getTime))
  }


  def canWeDumpExcessEnergyToGrid(batteryPercentage: Double, CurrentGridPull:Double)= {
    //only disable charging battery if - Battery is above 96% and we are not pulling from the grid
    if(!gridDumpEnabled && batteryPercentage >= 96.0 && CurrentGridPull <= 400.0) //required SOC to be 95%
      {
        //disable changing at send excess to grid by setting now as the changing window
        alpha.setSystemSettings(AlphaESSUpdateChargeConfigInfo.from(alpha.getSystemSettings()).copy(timeChaf2="07:00",timeChae2 = "23:00"))
        gridDumpEnabled = true
        logger.info("Battery charging Period 2 enable - battery at "+batteryPercentage+"%, so dumping excess to grid")
      }
    //if we pull from the grid - stop and use the battery
    if(gridDumpEnabled && (CurrentGridPull > 400.0))
      {
        //enable normal battery use by clearing this changing window
        alpha.setSystemSettings(AlphaESSUpdateChargeConfigInfo.from(alpha.getSystemSettings()).copy(timeChaf2="00:00",timeChae2 = "00:00"))
        gridDumpEnabled= false
        logger.info("Battery charging Period 2 reset")
      }
  }
}
