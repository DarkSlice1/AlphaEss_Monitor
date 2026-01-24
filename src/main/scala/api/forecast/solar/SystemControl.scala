package api.forecast.solar


import api.alpha.AlphaObjectMapper.{AlphaESSUpdateChargeConfigInfo, AlphaEssFeedStrategyConfig, AlphaEssFeedStrategyData, FeedStrategyVO}
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
      logger.info("Battery charging Period 2 to - 00:00 - 00:00")
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
    if(!gridDumpEnabled && batteryPercentage >= 96.0 && CurrentGridPull <= 400.0 && CurrentGridPull != 0.0) //required SOC to be 95%
      {
        //disable changing at send excess to grid by setting now as the changing window
        alpha.setSystemSettings(AlphaESSUpdateChargeConfigInfo.from(alpha.getSystemSettings()).copy(timeChaf2="07:00",timeChae2 = "23:00"))
        gridDumpEnabled = true
        logger.info("Battery charging Period 2 to - 07:00 - 23:00 - battery at "+batteryPercentage+"%, so dumping excess to grid")
      }
    //if we pull from the grid - stop and use the battery
    if(gridDumpEnabled && (CurrentGridPull > 400.0))
      {
        //enable normal battery use by clearing this changing window
        alpha.setSystemSettings(AlphaESSUpdateChargeConfigInfo.from(alpha.getSystemSettings()).copy(timeChaf2="00:00",timeChae2 = "00:00"))
        gridDumpEnabled= false
        logger.info("Battery charging Period 2 to - 00:00 - 00:00")
      }
  }

  /**
   * We want to dump the batter to the Grid in a Safe manner
   * we have 22k to work with at a 5kw per hour drain
   * battery charging starts at 2am and drops to a max of 10%
   * Starting at 23:00 we can start dumping depending on % percentage.
   * Let's review every few seconds and adjust to maximize drain before 2am but maintain enough charge for usagae
   * @param batteryPercentage
   * @return
   */
  def canWeDumpBatteryToGrid(batteryPercentage: Double) ={
    try {
      logger.info("Reviewing FIT Options")

      Calendar.getInstance().get(Calendar.HOUR_OF_DAY) match {
        case  23 => {
          //at 11pm
          //battery at 90%+ Drain at 5kw
          if (batteryPercentage > 90) {alpha.setFeedStrategy(UpdateFITConfig(1, 15, "23:00", "23:59", 5000))}
          //battery at 80%+ Drain at 4kw
          if (batteryPercentage > 80) {
            alpha.setFeedStrategy(UpdateFITConfig(1, 15, "23:00", "23:59", 4000))
          }
          //Battery at 70%+ Drain at 3kw
          if (batteryPercentage > 70) {
            alpha.setFeedStrategy(UpdateFITConfig(1, 15, "23:00", "23:59", 3000))
          }
          //Battery at 60%+ Drain at 2kw
          if (batteryPercentage > 60) {
            alpha.setFeedStrategy(UpdateFITConfig(1, 15, "23:00", "23:59", 2000))
          }
          //Battery at 50%+ Drain at 1kw
          if (batteryPercentage > 50) {
            alpha.setFeedStrategy(UpdateFITConfig(1, 15, "23:00", "23:59", 1000))
          }
          //Battery less than 50% - DON'T DRAIN
        }
        case 0 => {
          //at 00:00
          //battery at 90%+ Drain at 5kw
          if (batteryPercentage > 90) {
            alpha.setFeedStrategy(UpdateFITConfig(1, 15, "00:00", "00:59", 5000))
          }
          //battery at 80%+ Drain at 5kw
          if (batteryPercentage > 80) {
            alpha.setFeedStrategy(UpdateFITConfig(1, 15, "00:00", "00:59", 5000))
          }
          //Battery at 70%+ Drain at 5kw
          if (batteryPercentage > 70) {
            alpha.setFeedStrategy(UpdateFITConfig(1, 15, "00:00", "00:59", 5000))
          }
          //Battery at 60%+ Drain at 4kw
          if (batteryPercentage > 60) {
            alpha.setFeedStrategy(UpdateFITConfig(1, 15, "00:00", "00:59", 4000))
          }
          //Battery at 50%+ Drain at 3kw
          if (batteryPercentage > 50) {
            alpha.setFeedStrategy(UpdateFITConfig(1, 15, "00:00", "00:59", 3000))
          }
          //Battery at 40%+ Drain at 2kw
          if (batteryPercentage > 40) {
            alpha.setFeedStrategy(UpdateFITConfig(1, 15, "00:00", "00:59", 2000))
          }
          //Battery less than 40% - DON'T DRAIN
        }
        case 1 => {
          Calendar.getInstance().get(Calendar.MINUTE) match {
            case minute if minute < 30 => {
              //at 01:00
              //battery at 90%+ Drain at 5kw
              if (batteryPercentage > 90) {
                alpha.setFeedStrategy(UpdateFITConfig(1, 15, "01:00", "01:30", 5000))
              }
              //battery at 80%+ Drain at 5kw
              if (batteryPercentage > 80) {
                alpha.setFeedStrategy(UpdateFITConfig(1, 15, "01:00", "01:30", 5000))
              }
              //Battery at 70%+ Drain at 5kw
              if (batteryPercentage > 70) {
                alpha.setFeedStrategy(UpdateFITConfig(1, 15, "01:00", "01:30", 5000))
              }
              //Battery at 60%+ Drain at 5kw
              if (batteryPercentage > 60) {
                alpha.setFeedStrategy(UpdateFITConfig(1, 15, "01:00", "01:30", 5000))
              }
              //Battery at 50%+ Drain at 5kw
              if (batteryPercentage > 50) {
                alpha.setFeedStrategy(UpdateFITConfig(1, 15, "01:00", "01:30", 5000))
              }
              //Battery at 40%+ Drain at 5kw
              if (batteryPercentage > 40) {
                alpha.setFeedStrategy(UpdateFITConfig(1, 15, "01:00", "01:30", 5000))
              }
              //Battery at 30%+ Drain at 4kw
              if (batteryPercentage > 30) {
                alpha.setFeedStrategy(UpdateFITConfig(1, 15, "01:00", "01:30", 4000))
              }
            }
            case _ =>
              {
                //at 01:30+
                //Battery at 20%+ Drain at 2kw
                if (batteryPercentage > 20) {
                  alpha.setFeedStrategy(UpdateFITConfig(1, 15, "01:30", "01:59", 2000))
                }
                //Battery at 10%+ Drain at 0.5kw
                if (batteryPercentage > 10) {
                  alpha.setFeedStrategy(UpdateFITConfig(1, 10, "01:30", "01:59", 500))
                }
                //Battery less than 10% - DON'T DRAIN
              }
            //Battery less than 30% - DON'T DRAIN
          }
          logger.info("Updated FIT Options")
        }
        case _ =>
        alpha.setFeedStrategy(UpdateFITConfig(0, 15, "00:00", "00:01", 5000))
      }
    }
    def UpdateFITConfig(Enabled : Int, percentage :BigDecimal, start: String, end : String, FITPower:Int) : AlphaEssFeedStrategyConfig= {
      AlphaEssFeedStrategyConfig(Enabled,percentage, alpha.systemId,  List(FeedStrategyVO.from(alpha.getFeedStrategyList().feedStrategyVOList.head).copy(start=start,end = end,feedPower = FITPower)), 0)
    }

  }
}
