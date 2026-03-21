package api.forecast.solar


import api.alpha.AlphaObjectMapper.{AlphaESSUpdateChargeConfigInfo, AlphaESSUpdateChargeCycleInfo, AlphaEssFeedStrategyConfig, AlphaEssFeedStrategyData, FeedStrategyVO}
import api.alpha.alpha
import api.myenergi.{myenergi_eddie, myenergi_zappie}
import com.typesafe.scalalogging.LazyLogging

import java.util.Calendar

class SystemControl(alpha: alpha, zappi:myenergi_zappie, eddi:myenergi_eddie, forecast:SolarForecast) extends LazyLogging {

  private var gridDumpEnabled = false
  private var fitEnabled = false

  def ResetSync() ={

    try {
      //disable grid dump - default state
      val base = alpha.getSystemSettingsV2
      alpha.setSystemSettingsV2(
        AlphaESSUpdateChargeCycleInfo.from(
          base.copy(
            dayChargeTimeList =
              base.dayChargeTimeList.updated(
                base.dayChargeTimeList.size - 1,
                base.dayChargeTimeList.last.copy(
                  beginTime = "2:00",
                  endTime = "5:59"
                )
              )
          ),
          alpha.systemId
        )
      )
      gridDumpEnabled = false
      logger.info("Battery charging Period 2 to - 02:00 - 05:59")

      //disable fit - default state
      alpha.setFeedStrategy(UpdateFITConfig(0, 15, "00:00", "00:00", 500))
      fitEnabled = false
      logger.info("FIT Settings Disabled")

    }
    catch {
      case _:Exception =>  logger.error("Error in Reset Sync job")
    }
  }

  def setSystemSettingsBasedOnGeneratedForecast(): Unit = {
    val todaysForecast = forecast.getTodaysForcast()
    forecast.publishTodaysForcast(todaysForecast)
    logger.info("Publish forecasting Metrics")
  }



  def canWeDumpExcessEnergyToGrid(batteryPercentage: Double, CurrentGridPull:Double)= {
    //only disable charging battery if - Battery is above 96% and we are not pulling from the grid
    if(!gridDumpEnabled && batteryPercentage >= 96.0 && CurrentGridPull <= 400.0 && CurrentGridPull != 0.0) //required SOC to be 95%
      {
        //disable changing at send excess to grid by setting now as the changing window
        val base = alpha.getSystemSettingsV2
        alpha.setSystemSettingsV2(
          AlphaESSUpdateChargeCycleInfo.from(
            base.copy(
              dayChargeTimeList =
                base.dayChargeTimeList.updated(
                  base.dayChargeTimeList.size - 1,
                  base.dayChargeTimeList.last.copy(
                    beginTime = "07:00",
                    endTime   = "23:59"
                  )
                )
            ),
            alpha.systemId
          )
        )
        gridDumpEnabled = true
        logger.info("Battery charging Period 2 to - 07:00 - 23:59 - battery at "+batteryPercentage+"%, so dumping excess to grid")
      }
    //if we pull from the grid - stop and use the battery
    if(gridDumpEnabled && (CurrentGridPull > 400.0))
      {
        //enable normal battery use by clearing this changing window
        ResetSync()
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
      var startTime = "00:00"
      var endTime = "00:00"
      var percentage = 15
      var enabled = 0
      var watts = 0

      Calendar.getInstance().get(Calendar.HOUR_OF_DAY) match {
        case 23 =>
          //at 11pm
          batteryPercentage match {
            case batteryPercentage if (batteryPercentage < 100 && batteryPercentage > 91) =>
              enabled = 1; percentage = 90; startTime = "23:00"; endTime = "23:59"; watts = 5000

            case batteryPercentage if (batteryPercentage < 91 && batteryPercentage > 81) =>
              enabled = 1; percentage = 80; startTime = "23:00"; endTime = "23:59"; watts = 4000

            case batteryPercentage if (batteryPercentage < 81 && batteryPercentage > 71) =>
              enabled = 1; percentage = 70; startTime = "23:00"; endTime = "23:59"; watts = 3000

            case batteryPercentage if (batteryPercentage < 71 && batteryPercentage > 61) =>
              enabled = 1; percentage = 60; startTime = "23:00"; endTime = "23:59"; watts = 2000

            case batteryPercentage if (batteryPercentage < 61 && batteryPercentage > 51) =>
              enabled = 1; percentage = 50; startTime = "23:00"; endTime = "23:59"; watts = 1000

            case _ =>
              enabled = 0; percentage = 15; startTime = "00:00"; endTime = "00:01"; watts = 500
          }
        case 0 =>
          //at 00:00
          batteryPercentage match {
            case batteryPercentage if (batteryPercentage < 100 && batteryPercentage > 71) =>
              enabled = 1; percentage = 70; startTime = "00:00"; endTime = "00:59"; watts = 5000

            case batteryPercentage if (batteryPercentage < 71 && batteryPercentage > 61) =>
              enabled = 1; percentage = 60; startTime = "00:00"; endTime = "00:59"; watts = 4000

            case batteryPercentage if (batteryPercentage < 61 && batteryPercentage > 51) =>
              enabled = 1; percentage = 50; startTime = "00:00"; endTime = "00:59"; watts = 3000

            case batteryPercentage if (batteryPercentage < 51 && batteryPercentage > 41) =>
              enabled = 1; percentage = 40; startTime = "00:00"; endTime = "00:59"; watts = 2000

            case _ =>
              enabled = 0; percentage = 15; startTime = "00:00"; endTime = "00:01"; watts = 500
          }
        case 1 =>
          Calendar.getInstance().get(Calendar.MINUTE) match {
            case minute if minute < 30 =>
              //at 01:00
              batteryPercentage match {
                case batteryPercentage if (batteryPercentage < 100 && batteryPercentage > 41) =>
                  enabled = 1; percentage = 40; startTime = "01:00"; endTime = "01:30"; watts = 5000

                case batteryPercentage if (batteryPercentage < 41 && batteryPercentage > 31) =>
                  enabled = 1; percentage = 30; startTime = "01:00"; endTime = "01:30"; watts = 4000
              }
            case _ =>
              //at 01:30+
              batteryPercentage match {
                case batteryPercentage if (batteryPercentage < 100 && batteryPercentage > 21) =>
                  enabled = 1; percentage = 20; startTime = "01:30"; endTime = "01:59"; watts = 2000

                case batteryPercentage if (batteryPercentage < 21 && batteryPercentage > 11) =>
                  enabled = 1; percentage = 10; startTime = "01:30"; endTime = "01:59"; watts = 500
              }
          }
        case _ =>
            enabled = 0; percentage = 15; startTime = "00:00"; endTime = "00:01"; watts = 500
      }
      if (enabled == 1) {
        alpha.setFeedStrategy(UpdateFITConfig(enabled, percentage, startTime, endTime, watts))
        logger.info("Updated FIT Options, battery charge = " + batteryPercentage + ", enabled =" + enabled + ", percentage = " + percentage + ", start time = " + startTime + ", end time = " + endTime + ", wattage = " + watts)
        fitEnabled = true
      }
      if(fitEnabled && enabled == 0) {
        alpha.setFeedStrategy(UpdateFITConfig(enabled, percentage, startTime, endTime, watts))
        logger.info("Updated FIT Options, battery charge = " + batteryPercentage + ", enabled =" + enabled + ", percentage = " + percentage + ", start time = " + startTime + ", end time = " + endTime + ", wattage = " + watts)
        fitEnabled = false
      }
    }
    catch {
      case ex:Exception =>  logger.error("Error in managing FIT "+ex.printStackTrace())
    }
  }

  def UpdateFITConfig(Enabled : Int, percentage :BigDecimal, start: String, end : String, FITPower:Int) : AlphaEssFeedStrategyConfig= {
    AlphaEssFeedStrategyConfig(Enabled,percentage, alpha.systemId,  List(FeedStrategyVO.from(alpha.getFeedStrategyList.feedStrategyVOList.head).copy(start=start,end = end,feedPower = FITPower)), 0)
  }
}
