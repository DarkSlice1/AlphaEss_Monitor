package api.alpha

import api.alpha.AlphaObjectMapper.AlphaMetrics
import com.typesafe.config.Config
import metrics.KamonMetrics

import java.util.Calendar

class reportHome(config: Config, reporterKamon : KamonMetrics) {
  val syn_name = config.getString("alphaess.system_sn")

  var DailySolarGeneration : Double = 0
  private val ppv1 = reporterKamon.ppv1.add().withTag("sys_name",syn_name)
  private val ppv2 = reporterKamon.ppv2.add().withTag("sys_name",syn_name)
  private val ppv3 = reporterKamon.ppv3.add().withTag("sys_name",syn_name)
  private val ppv4 = reporterKamon.ppv4.add().withTag("sys_name",syn_name)
  private val preal_l1 = reporterKamon.preal_l1.add().withTag("sys_name",syn_name)
  private val preal_l2 = reporterKamon.preal_l2.add().withTag("sys_name",syn_name)
  private val preal_l3 = reporterKamon.preal_l3.add().withTag("sys_name",syn_name)
  private val gridPull_l1 = reporterKamon.gridPull_l1.add().withTag("sys_name",syn_name)
  private val gridPush_l1 = reporterKamon.gridPush_l1.add().withTag("sys_name",syn_name)
  private val pmeter_l1 = reporterKamon.pmeter_l1.add().withTag("sys_name",syn_name)
  private val pmeter_l2 = reporterKamon.pmeter_l2.add().withTag("sys_name",syn_name)
  private val pmeter_l3 = reporterKamon.pmeter_l3.add().withTag("sys_name",syn_name)
  private val pmeter_dc = reporterKamon.pmeter_dc.add().withTag("sys_name",syn_name)
  private val soc = reporterKamon.soc.add().withTag("sys_name",syn_name)
  private val factory_flag = reporterKamon.factory_flag.add().withTag("sys_name",syn_name)
  private val pbatChargeGauge = reporterKamon.pbatChargeGauge.add().withTag("sys_name",syn_name)
  private val pbatDischargeGauge = reporterKamon.pbatDischargeGauge.add().withTag("sys_name",syn_name)
  private val sva = reporterKamon.sva.add().withTag("sys_name",syn_name)
  private val varac = reporterKamon.varac.add().withTag("sys_name",syn_name)
  private val vardc = reporterKamon.vardc.add().withTag("sys_name",syn_name)
  private val ev1_power = reporterKamon.ev1_power.add().withTag("sys_name",syn_name)
  private val ev1_chgenergy_real = reporterKamon.ev1_chgenergy_real.add().withTag("sys_name",syn_name)
  private val ev1_mode = reporterKamon.ev1_mode.add().withTag("sys_name",syn_name)
  private val ev2_power = reporterKamon.ev2_power.add().withTag("sys_name",syn_name)
  private val ev2_chgenergy_real = reporterKamon.ev2_chgenergy_real.add().withTag("sys_name",syn_name)
  private val ev2_mode = reporterKamon.ev2_mode.add().withTag("sys_name",syn_name)
  private val ev3_power = reporterKamon.ev3_power.add().withTag("sys_name",syn_name)
  private val ev3_chgenergy_real = reporterKamon.ev3_chgenergy_real.add().withTag("sys_name",syn_name)
  private val ev3_mode = reporterKamon.ev3_mode.add().withTag("sys_name",syn_name)
  private val ev4_power = reporterKamon.ev4_power.add().withTag("sys_name",syn_name)
  private val ev4_chgenergy_real = reporterKamon.ev4_chgenergy_real.add().withTag("sys_name",syn_name)
  private val ev4_mode = reporterKamon.ev4_mode.add().withTag("sys_name",syn_name)
  private val poc_meter_l1 = reporterKamon.poc_meter_l1.add().withTag("sys_name",syn_name)
  private val poc_meter_l2 = reporterKamon.poc_meter_l2.add().withTag("sys_name",syn_name)
  private val poc_meter_l3 = reporterKamon.poc_meter_l3.add().withTag("sys_name",syn_name)
  private val houseLoad = reporterKamon.houseLoad.add().withTag("sys_name",syn_name)

  //https://github.com/liqun2013/alphaess-webapi/blob/93539b332f2be17240f7359be2f0c51deda06d6c/AlphaEssWeb.Api_V2.Model/Dtos/PowerDataDto.cs
  def write(metrics : AlphaMetrics): Unit = {

    //House load calc
    val solarGeneration = (metrics.ppv)
    val gridConsumption = (metrics.pgrid)
    val batteryConsumption = metrics.pbat

    //Handle Grid flow Metrics
    GridFlowMetrics(metrics, gridConsumption)
    //Handle Battery flow Metrics
    BatteryFlowMetrics(metrics)
    //handle Solar Generator
    SolarFlowMetrics(metrics,solarGeneration)

    houseLoad.update(CheckForZero(solarGeneration + gridConsumption + batteryConsumption))

    //Misc Metrics Below

    //preal_l1 is the output of the inverter "Inverter L1 real-time output power, this parameter has positive and negative"
    //should be equal to the PPV's minus the discharge of the battery

    soc.update(CheckForZero(metrics.soc))

    //guessing this is the total Wattage the inverter is pulling to do its job ???
    //need to confirm - not sure how - CT-Clamp???

  }

  //seems metrics are given in double we * 10 here and divide by 10 on DD
  def CheckForZero(value:Double):Long = {
    if (value == 0)
      0
    else
      (value * 10).toLong
  }

  def GridFlowMetrics(metrics: AlphaMetrics, gridConsumption: Double): Unit = {
    //Grid Push or Pull ?
    if (metrics.pgrid > 0) {
      //update our counter for tracking grid pull
      reporterKamon.totalGridConsumption.increment(CheckForZero(gridConsumption), "sys_name", syn_name)
      //update our gauge for tracking grid pull
      gridPull_l1.update(CheckForZero(metrics.pgrid))
      // set our grid push to 0
      gridPush_l1.update(0)
      //add cost metric
      CostPerKwMetric(metrics.pgrid)
    } else {
      //update our counter for tracking grid push
      reporterKamon.totalGridPush.increment(CheckForZero(Math.abs(gridConsumption)), "sys_name", syn_name)
      //update our counter for tracking grid push
      gridPush_l1.update(CheckForZero(Math.abs(metrics.pgrid)))
      // set our grid pull to 0
      gridPull_l1.update(0)
    }
  }

  def CostPerKwMetric(gridConsumption: Double):Unit = {

    //clear values first
    reporterKamon.gridPull_CostHour_00_01.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_01_02.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_02_03.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_03_04.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_04_05.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_05_06.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_06_07.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_07_08.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_08_09.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_09_10.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_10_11.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_11_12.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_12_13.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_13_14.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_14_15.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_15_16.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_16_17.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_17_18.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_18_19.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_19_20.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_20_21.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_21_22.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_22_23.increment(0,"sys_name", syn_name)
    reporterKamon.gridPull_CostHour_23_00.increment(0,"sys_name", syn_name)

    (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)).toString() match
    {
      case "0" => reporterKamon.gridPull_CostHour_00_01.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "1" => reporterKamon.gridPull_CostHour_01_02.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "2" => reporterKamon.gridPull_CostHour_02_03.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "3" => reporterKamon.gridPull_CostHour_03_04.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "4" => reporterKamon.gridPull_CostHour_04_05.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "5" => reporterKamon.gridPull_CostHour_05_06.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "6" => reporterKamon.gridPull_CostHour_06_07.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "7" => reporterKamon.gridPull_CostHour_07_08.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "8" => reporterKamon.gridPull_CostHour_08_09.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "9" => reporterKamon.gridPull_CostHour_09_10.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "10" => reporterKamon.gridPull_CostHour_10_11.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "11" => reporterKamon.gridPull_CostHour_11_12.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "12" => reporterKamon.gridPull_CostHour_12_13.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "13" => reporterKamon.gridPull_CostHour_13_14.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "14" => reporterKamon.gridPull_CostHour_14_15.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "15" => reporterKamon.gridPull_CostHour_15_16.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "16" => reporterKamon.gridPull_CostHour_16_17.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "17" => reporterKamon.gridPull_CostHour_17_18.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "18" => reporterKamon.gridPull_CostHour_18_19.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "19" => reporterKamon.gridPull_CostHour_19_20.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "20" => reporterKamon.gridPull_CostHour_20_21.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "21" => reporterKamon.gridPull_CostHour_21_22.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "22" => reporterKamon.gridPull_CostHour_22_23.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
      case "23" => reporterKamon.gridPull_CostHour_23_00.increment(CheckForZero(gridConsumption),"sys_name", syn_name)
    }
  }

  def BatteryFlowMetrics(metrics: AlphaMetrics): Unit = {
    //discharging battery
    if (metrics.pbat > 0) {
      reporterKamon.pbatDischargeCounter.increment(CheckForZero(metrics.pbat), "sys_name", syn_name)
      pbatDischargeGauge.update(CheckForZero(metrics.pbat))
      pbatChargeGauge.update(0)
    }

    //charging battery
    else if (metrics.pbat < 0) {

      //battery still get charged when full (apparently)
      reporterKamon.pbatChargeCounter.increment(Math.abs(CheckForZero(metrics.pbat)), "sys_name", syn_name)
      pbatChargeGauge.update(Math.abs(CheckForZero(metrics.pbat)))
      pbatDischargeGauge.update(0)
    }

    //zero metrics for charging / discharging if not active
    else {
      pbatDischargeGauge.update(0)
      pbatChargeGauge.update(0)
    }
  }

  def SolarFlowMetrics(metrics: AlphaMetrics, solarGeneration : Double): Unit =
  {
    ppv1.update(CheckForZero(metrics.ppv))

    DailySolarGeneration += solarGeneration
    reporterKamon.totalSolarGeneration.increment(CheckForZero(solarGeneration),"sys_name", syn_name)
  }
}
