package api.alpha

import api.alpha.AlphaObjectMapper.AlphaMetrics
import metrics.KamonMetrics

class reportHome(syn_name: String) {

  val reporterKamon = new KamonMetrics()
  private val ppv1 = reporterKamon.ppv1.add().withTag("sys_name",syn_name)
  private val ppv2 = reporterKamon.ppv2.add().withTag("sys_name",syn_name)
  private val ppv3 = reporterKamon.ppv3.add().withTag("sys_name",syn_name)
  private val ppv4 = reporterKamon.ppv4.add().withTag("sys_name",syn_name)
  private val preal_l1 = reporterKamon.preal_l1.add().withTag("sys_name",syn_name)
  private val preal_l2 = reporterKamon.preal_l2.add().withTag("sys_name",syn_name)
  private val preal_l3 = reporterKamon.preal_l3.add().withTag("sys_name",syn_name)
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


  def write(metrics : AlphaMetrics): Unit = {
    ppv1.update(CheckForZero(metrics.ppv1))
    ppv2.update(CheckForZero(metrics.ppv2))
    ppv3.update(CheckForZero(metrics.ppv3))
    ppv4.update(CheckForZero(metrics.ppv4))
    preal_l1.update(CheckForZero(metrics.preal_l1))
    preal_l2.update(CheckForZero(metrics.preal_l2))
    preal_l3.update(CheckForZero(metrics.preal_l3))
    pmeter_l1.update(CheckForZero(metrics.pmeter_l1))
    pmeter_l2.update(CheckForZero(metrics.pmeter_l2))
    pmeter_l3.update(CheckForZero(metrics.pmeter_l3))
    pmeter_dc.update(CheckForZero(metrics.pmeter_dc))
    soc.update(CheckForZero(metrics.soc))
    factory_flag.update(metrics.factory_flag)
    sva.update(CheckForZero(metrics.sva))
    varac.update(CheckForZero(metrics.varac))
    vardc.update(CheckForZero(metrics.vardc))
    ev1_power.update(metrics.ev1_power)
    ev1_chgenergy_real.update(CheckForZero(metrics.ev1_chgenergy_real))
    ev1_mode.update(metrics.ev1_mode)
    ev2_power.update(metrics.ev2_power)
    ev2_chgenergy_real.update(CheckForZero(metrics.ev2_chgenergy_real))
    ev2_mode.update(metrics.ev2_mode)
    ev3_power.update(metrics.ev3_power)
    ev3_chgenergy_real.update(CheckForZero(metrics.ev3_chgenergy_real))
    ev3_mode.update(metrics.ev3_mode)
    ev4_power.update(metrics.ev4_power)
    ev4_chgenergy_real.update((metrics.ev4_chgenergy_real))
    ev4_mode.update(metrics.ev4_mode)
    poc_meter_l1.update(CheckForZero(metrics.poc_meter_l1))
    poc_meter_l2.update(CheckForZero(metrics.poc_meter_l2))
    poc_meter_l3.update(CheckForZero(metrics.poc_meter_l3))

    if(metrics.pbat>0) {
      reporterKamon.pbatDischargeCounter.increment((metrics.pbat * 10).toLong,"sys_name", syn_name)
      pbatDischargeGauge.update((metrics.pbat * 10).toLong)
    }
    else {
      // don't increment if battery charge as part of SOC if battery is full
      if(metrics.soc != 100) {
        reporterKamon.pbatChargeCounter.increment(Math.abs((metrics.pbat * 10).toLong), "sys_name", syn_name)
      }
      //battery still get charged when full (apparently)
      pbatChargeGauge.update(Math.abs((metrics.pbat * 10).toLong))
    }

    //zero metrics for charging / discharging if not active
    if(metrics.pbat == 0)
      {
        pbatDischargeGauge.update(0)
        pbatChargeGauge.update(0)
      }
    reporterKamon.invertorPower.increment((metrics.varac * 10).toLong, "sys_name", syn_name)

    //println("Metrics Pushed")
  }

  def CheckForZero(value:Double):Long = {
    if (value == 0)
      0
    else
      (value * 10).toLong
  }
}
