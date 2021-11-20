package Api

import Api.objectMapper.AlphaMetrics
import kamon.Tags
import metrics.KamonMetrics

class reportHome(syn_name: String) {

  val reporterKamon = new KamonMetrics()
  private val ppv1 = reporterKamon.ppv1.add().refine(Map("sys_name"->syn_name))
  private val ppv2 = reporterKamon.ppv2.add().refine(Map("sys_name"->syn_name))
  private val ppv3 = reporterKamon.ppv3.add().refine(Map("sys_name"->syn_name))
  private val ppv4 = reporterKamon.ppv4.add().refine(Map("sys_name"->syn_name))
  private val preal_l1 = reporterKamon.preal_l1.add().refine(Map("sys_name"->syn_name))
  private val preal_l2 = reporterKamon.preal_l2.add().refine(Map("sys_name"->syn_name))
  private val preal_l3 = reporterKamon.preal_l3.add().refine(Map("sys_name"->syn_name))
  private val pmeter_l1 = reporterKamon.pmeter_l1.add().refine(Map("sys_name"->syn_name))
  private val pmeter_l2 = reporterKamon.pmeter_l2.add().refine(Map("sys_name"->syn_name))
  private val pmeter_l3 = reporterKamon.pmeter_l3.add().refine(Map("sys_name"->syn_name))
  private val pmeter_dc = reporterKamon.pmeter_dc.add().refine(Map("sys_name"->syn_name))
  private val soc = reporterKamon.soc.add().refine(Map("sys_name"->syn_name))
  private val factory_flag = reporterKamon.factory_flag.add().refine(Map("sys_name"->syn_name))
  private val pbat = reporterKamon.pbat.add().refine(Map("sys_name"->syn_name))
  private val sva = reporterKamon.sva.add().refine(Map("sys_name"->syn_name))
  private val varac = reporterKamon.varac.add().refine(Map("sys_name"->syn_name))
  private val vardc = reporterKamon.vardc.add().refine(Map("sys_name"->syn_name))
  private val ev1_power = reporterKamon.ev1_power.add().refine(Map("sys_name"->syn_name))
  private val ev1_chgenergy_real = reporterKamon.ev1_chgenergy_real.add().refine(Map("sys_name"->syn_name))
  private val ev1_mode = reporterKamon.ev1_mode.add().refine(Map("sys_name"->syn_name))
  private val ev2_power = reporterKamon.ev2_power.add().refine(Map("sys_name"->syn_name))
  private val ev2_chgenergy_real = reporterKamon.ev2_chgenergy_real.add().refine(Map("sys_name"->syn_name))
  private val ev2_mode = reporterKamon.ev2_mode.add().refine(Map("sys_name"->syn_name))
  private val ev3_power = reporterKamon.ev3_power.add().refine(Map("sys_name"->syn_name))
  private val ev3_chgenergy_real = reporterKamon.ev3_chgenergy_real.add().refine(Map("sys_name"->syn_name))
  private val ev3_mode = reporterKamon.ev3_mode.add().refine(Map("sys_name"->syn_name))
  private val ev4_power = reporterKamon.ev4_power.add().refine(Map("sys_name"->syn_name))
  private val ev4_chgenergy_real = reporterKamon.ev4_chgenergy_real.add().refine(Map("sys_name"->syn_name))
  private val ev4_mode = reporterKamon.ev4_mode.add().refine(Map("sys_name"->syn_name))
  private val poc_meter_l1 = reporterKamon.poc_meter_l1.add().refine(Map("sys_name"->syn_name))
  private val poc_meter_l2 = reporterKamon.poc_meter_l2.add().refine(Map("sys_name"->syn_name))
  private val poc_meter_l3 = reporterKamon.poc_meter_l3.add().refine(Map("sys_name"->syn_name))

  //Battery Charge = SoC (State Of Charge)
  //Grid Pull = pmeter_l1
  //Solar = ppv1 + ppv2
  //House load = Solar + Grid Pull


  def write(metrics : AlphaMetrics): Unit = {
    ppv1.set((metrics.ppv1 * 10).toLong)
    ppv2.set((metrics.ppv2 * 10).toLong)
    ppv3.set((metrics.ppv3 * 10).toLong)
    ppv4.set((metrics.ppv4 * 10).toLong)
    preal_l1.set((metrics.preal_l1 * 10).toLong)
    preal_l2.set((metrics.preal_l2 * 10).toLong)
    preal_l3.set((metrics.preal_l3 * 10).toLong)
    pmeter_l1.set((metrics.pmeter_l1 * 10).toLong)
    pmeter_l2.set((metrics.pmeter_l2 * 10).toLong)
    pmeter_l3.set((metrics.pmeter_l3 * 10).toLong)
    pmeter_dc.set((metrics.pmeter_dc * 10).toLong)
    soc.set((metrics.soc * 10).toLong)
    factory_flag.set(metrics.factory_flag)
    pbat.set((metrics.pbat * 10).toLong)
    sva.set((metrics.sva * 10).toLong)
    varac.set((metrics.varac * 10).toLong)
    vardc.set((metrics.vardc * 10).toLong)
    ev1_power.set(metrics.ev1_power)
    ev1_chgenergy_real.set((metrics.ev1_chgenergy_real * 10).toLong)
    ev1_mode.set(metrics.ev1_mode)
    ev2_power.set(metrics.ev2_power)
    ev2_chgenergy_real.set((metrics.ev2_chgenergy_real * 10).toLong)
    ev2_mode.set(metrics.ev2_mode)
    ev3_power.set(metrics.ev3_power)
    ev3_chgenergy_real.set((metrics.ev3_chgenergy_real * 10).toLong)
    ev3_mode.set(metrics.ev3_mode)
    ev4_power.set(metrics.ev4_power)
    ev4_chgenergy_real.set((metrics.ev4_chgenergy_real * 10).toLong)
    ev4_mode.set(metrics.ev4_mode)
    poc_meter_l1.set((metrics.poc_meter_l1 * 10).toLong)
    poc_meter_l2.set((metrics.poc_meter_l2 * 10).toLong)
    poc_meter_l3.set((metrics.poc_meter_l3 * 10).toLong)

    println("Metrics Pushed")
  }
}
