package Api

import Api.objectMapper.{AlphaMetrics}
import metrics.KamonMetrics

class reportHome {

  val reporterKamon = new KamonMetrics()

  def write(metrics : AlphaMetrics): Unit = {
    reporterKamon.ppv1.record((metrics.ppv1 * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.ppv2.record((metrics.ppv2 * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.ppv3.record((metrics.ppv3 * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.ppv4.record((metrics.ppv4 * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.preal_l1.record((metrics.preal_l1 * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.preal_l2.record((metrics.preal_l2 * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.preal_l3.record((metrics.preal_l3 * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.pmeter_l1.record((metrics.pmeter_l1 * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.pmeter_l2.record((metrics.pmeter_l2 * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.pmeter_l3.record((metrics.pmeter_l3 * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.pmeter_dc.record((metrics.pmeter_dc * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.soc.record((metrics.soc * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.factory_flag.record(metrics.factory_flag, Map("sn_sys" -> metrics.sn))
    reporterKamon.pbat.record((metrics.pbat * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.sva.record((metrics.sva * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.varac.record((metrics.varac * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.vardc.record((metrics.vardc * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.ev1_power.record(metrics.ev1_power, Map("sn_sys" -> metrics.sn))
    reporterKamon.ev1_chgenergy_real.record((metrics.ev1_chgenergy_real * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.ev1_mode.record(metrics.ev1_mode, Map("sn_sys" -> metrics.sn))
    reporterKamon.ev2_power.record(metrics.ev2_power, Map("sn_sys" -> metrics.sn))
    reporterKamon.ev2_chgenergy_real.record((metrics.ev2_chgenergy_real * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.ev2_mode.record(metrics.ev2_mode, Map("sn_sys" -> metrics.sn))
    reporterKamon.ev3_power.record(metrics.ev3_power, Map("sn_sys" -> metrics.sn))
    reporterKamon.ev3_chgenergy_real.record((metrics.ev3_chgenergy_real * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.ev3_mode.record(metrics.ev3_mode, Map("sn_sys" -> metrics.sn))
    reporterKamon.ev4_power.record(metrics.ev4_power, Map("sn_sys" -> metrics.sn))
    reporterKamon.ev4_chgenergy_real.record((metrics.ev4_chgenergy_real * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.ev4_mode.record(metrics.ev4_mode, Map("sn_sys" -> metrics.sn))
    reporterKamon.poc_meter_l1.record((metrics.poc_meter_l1 * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.poc_meter_l2.record((metrics.poc_meter_l2 * 10).toLong, Map("sn_sys" -> metrics.sn))
    reporterKamon.poc_meter_l3.record((metrics.poc_meter_l3 * 10).toLong, Map("sn_sys" -> metrics.sn))
  }
}
