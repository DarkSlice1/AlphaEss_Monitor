package Api

import Api.AlphaObjectMapper.AlphaMetrics
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
  private val pbat = reporterKamon.pbat.add().withTag("sys_name",syn_name)
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
    ppv1.update((metrics.ppv1 * 10).toLong)
    ppv2.update((metrics.ppv2 * 10).toLong)
    ppv3.update((metrics.ppv3 * 10).toLong)
    ppv4.update((metrics.ppv4 * 10).toLong)
    preal_l1.update((metrics.preal_l1 * 10).toLong)
    preal_l2.update((metrics.preal_l2 * 10).toLong)
    preal_l3.update((metrics.preal_l3 * 10).toLong)
    pmeter_l1.update((metrics.pmeter_l1 * 10).toLong)
    pmeter_l2.update((metrics.pmeter_l2 * 10).toLong)
    pmeter_l3.update((metrics.pmeter_l3 * 10).toLong)
    pmeter_dc.update((metrics.pmeter_dc * 10).toLong)
    soc.update((metrics.soc * 10).toLong)
    factory_flag.update(metrics.factory_flag)
    pbat.update((metrics.pbat * 10).toLong)
    sva.update((metrics.sva * 10).toLong)
    varac.update((metrics.varac * 10).toLong)
    vardc.update((metrics.vardc * 10).toLong)
    ev1_power.update(metrics.ev1_power)
    ev1_chgenergy_real.update((metrics.ev1_chgenergy_real * 10).toLong)
    ev1_mode.update(metrics.ev1_mode)
    ev2_power.update(metrics.ev2_power)
    ev2_chgenergy_real.update((metrics.ev2_chgenergy_real * 10).toLong)
    ev2_mode.update(metrics.ev2_mode)
    ev3_power.update(metrics.ev3_power)
    ev3_chgenergy_real.update((metrics.ev3_chgenergy_real * 10).toLong)
    ev3_mode.update(metrics.ev3_mode)
    ev4_power.update(metrics.ev4_power)
    ev4_chgenergy_real.update((metrics.ev4_chgenergy_real * 10).toLong)
    ev4_mode.update(metrics.ev4_mode)
    poc_meter_l1.update((metrics.poc_meter_l1 * 10).toLong)
    poc_meter_l2.update((metrics.poc_meter_l2 * 10).toLong)
    poc_meter_l3.update((metrics.poc_meter_l3 * 10).toLong)

    if(metrics.pbat>0) {
      reporterKamon.pbatDischarge.increment((metrics.pbat * 10).toLong,"sys_name", syn_name)
    }
    else {
      if(metrics.soc != 100)// dont increment if battery is full
        reporterKamon.pbatCharge.increment(Math.abs((metrics.pbat * 10).toLong), "sys_name", syn_name)
    }

    reporterKamon.invertorPower.increment((metrics.varac * 10).toLong, "sys_name", syn_name)

    println("Metrics Pushed")
  }
}
