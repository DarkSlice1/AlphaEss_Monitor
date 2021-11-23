package metrics

import kamon.Kamon
import kamon.datadog.DatadogAgentReporter
import kamon.metric.MeasurementUnit
import kamon.tag.TagSet
import kamon.util.UnifiedMap

class KamonMetrics {

//Kamon.addReporter(new DatadogAgentReporter)

  trait Counter {
    val name: String

    def increment() = {
      Kamon.counter(name).withoutTags().increment()
    }

    def increment(TagName:String, TagValue:String) = {
      if (TagName.nonEmpty)
        Kamon.counter(name).withTag(TagName,TagValue).increment()
      else
        Kamon.counter(name).withoutTags().increment()
    }

    def increment(amount: Long, TagName:String, TagValue:String) = {
      if (TagName.nonEmpty)
        Kamon.counter(name).withTag(TagName,TagValue).increment(amount)
      else
        Kamon.counter(name).withoutTags.increment(amount)
    }

    def remove() = {
      Kamon.counter(name).withoutTags.remove()
    }

    def remove(TagName:String, TagValue:String) = {
      Kamon.counter(name).withTag(TagName,TagValue).remove()
    }
  }

  trait Histogram {
    val name: String

    def record(value: Long) = Kamon.histogram(name).withoutTags().record(value)

    def record(value: Long, TagName:String, TagValue:String) = Kamon.histogram(name).withTag(TagName,TagValue).record(value)

    def remove() = {
      Kamon.histogram(name).withoutTags().remove()
    }

    def remove(TagName:String, TagValue:String) = {
      Kamon.histogram(name).withTag(TagName,TagValue).remove()
    }
  }

  trait RangeSampler {
    val name: String

    def increment() = Kamon.rangeSampler(name).withoutTags().increment()

    def increment(TagName:String, TagValue:String) = Kamon.rangeSampler(name).withTag(TagName,TagValue).increment()

    def decrement() = Kamon.rangeSampler(name).withoutTags().decrement()

    def decrement(TagName:String, TagValue:String) = Kamon.rangeSampler(name).withTag(TagName,TagValue).decrement()

    def remove() = {
      Kamon.rangeSampler(name)
    }

    def remove(TagName:String, TagValue:String) = {
      Kamon.rangeSampler(name).withTag(TagName,TagValue).remove
    }
  }

  trait Gauge {
    val name: String

    def add() = Kamon.gauge(name)

    def add(TagName:String, TagValue:String) = Kamon.gauge(name).withTag(TagName,TagValue).update(0)

    def add(value: Double, TagName:String, TagValue:String) = Kamon.gauge(name).withTag(TagName,TagValue).update(value)

    def set(value: Long) = Kamon.gauge(name).withoutTags().update(value)

    def increment() = Kamon.gauge(name).withoutTags().increment()

    def increment(value: Long) = Kamon.gauge(name).withoutTags().increment(value)

    def decrement() = Kamon.gauge(name).withoutTags().decrement()

    def decrement(value: Long) = Kamon.gauge(name).withoutTags().decrement(value)

    def remove() = Kamon.gauge(name).withoutTags().remove()

    def remove(TagName:String, TagValue:String) = Kamon.gauge(name).withTag(TagName,TagValue).remove()

  }


  object ppv1 extends Gauge {
    val name = "alpha.ess.ppv1"
  }

  object ppv2 extends Gauge {
    val name = "alpha.ess.ppv2"
  }

  object ppv3 extends Gauge {
    val name = "alpha.ess.ppv3"
  }

  object ppv4 extends Gauge {
    val name = "alpha.ess.ppv4"
  }

  object preal_l1 extends Gauge {
    val name = "alpha.ess.preal_l1"
  }

  object preal_l2 extends Gauge {
    val name = "alpha.ess.preal_l2"
  }

  object preal_l3 extends Gauge {
    val name = "alpha.ess.preal_l3"
  }

  object pmeter_l1 extends Gauge {
    val name = "alpha.ess.pmeter_l1"
  }

  object pmeter_l2 extends Gauge {
    val name = "alpha.ess.pmeter_l2"
  }

  object pmeter_l3 extends Gauge {
    val name = "alpha.ess.pmeter_l3"
  }

  object pmeter_dc extends Gauge {
    val name = "alpha.ess.pmeter_dc"
  }

  object soc extends Gauge {
    val name = "alpha.ess.soc"
  }

  object factory_flag extends Gauge {
    val name = "alpha.ess.factory_flag"
  }

  object pbat extends Gauge {
    val name = "alpha.ess.pbat"
  }

  object pbatCharge extends Counter {
    val name = "alpha.ess.pbatCharge"
  }

  object pbatDischarge extends Counter {
    val name = "alpha.ess.pbatDischarge"
  }

  object invertorPower extends Counter {
    val name = "alpha.ess.invertorPower"
  }


  object sva extends Gauge {
    val name = "alpha.ess.sva"
  }

  object varac extends Gauge {
    val name = "alpha.ess.varac"
  }

  object vardc extends Gauge {
    val name = "alpha.ess.vardc"
  }

  object ev1_power extends Gauge {
    val name = "alpha.ess.ev1_power"
  }

  object ev1_chgenergy_real extends Gauge {
    val name = "alpha.ess.ev1_chgenergy_real"
  }

  object ev1_mode extends Gauge {
    val name = "alpha.ess.ev1_mode"
  }

  object ev2_power extends Gauge {
    val name = "alpha.ess.ev2_power"
  }

  object ev2_chgenergy_real extends Gauge {
    val name = "ev2_chgenergy_real"
  }

  object ev2_mode extends Gauge {
    val name = "alpha.ess.ev2_mode"
  }

  object ev3_power extends Gauge {
    val name = "alpha.ess.ev3_power"
  }

  object ev3_chgenergy_real extends Gauge {
    val name = "alpha.ess.ev3_chgenergy_real"
  }

  object ev3_mode extends Gauge {
    val name = "alpha.ess.ev3_mode"
  }

  object ev4_power extends Gauge {
    val name = "alpha.ess.ev4_power"
  }

  object ev4_chgenergy_real extends Gauge {
    val name = "alpha.ess.ev4_chgenergy_real"
  }

  object ev4_mode extends Gauge {
    val name = "alpha.ess.ev4_mode"
  }

  object poc_meter_l1 extends Gauge {
    val name = "alpha.ess.poc_meter_l1"
  }

  object poc_meter_l2 extends Gauge {
    val name = "alpha.ess.poc_meter_l2"
  }

  object poc_meter_l3 extends Gauge {
    val name = "alpha.ess.poc_meter_l3"
  }


}
