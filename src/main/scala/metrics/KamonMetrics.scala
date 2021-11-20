package metrics

import kamon.Kamon
import kamon.datadog.DatadogAgentReporter
import kamon.metric.MeasurementUnit

class KamonMetrics {

//Kamon.addReporter(new DatadogAgentReporter)

  trait Counter {
    val name: String

    def increment() = {
      Kamon.counter(name).increment()
    }

    def increment(tags: Option[Map[String, String]]) = {
      if (tags.isDefined)
        Kamon.counter(name).refine(tags.get).increment()
      else
        Kamon.counter(name).increment()
    }

    def increment(amount: Long, tags: Option[Map[String, String]] = None) = {
      if (tags.isDefined)
        Kamon.counter(name).refine(tags.get).increment(amount)
      else
        Kamon.counter(name).increment(amount)
    }

    def remove() = {
      Kamon.counter(name).remove()
    }

    def remove(tags: Map[String, String]) = {
      Kamon.counter(name).remove(tags)
    }
  }

  trait Histogram {
    val name: String

    def record(value: Long) = Kamon.histogram(name).record(value)

    def record(value: Long, tags: Map[String, String]) = Kamon.histogram(name).refine(tags).record(value)

    def remove() = {
      Kamon.histogram(name).remove()
    }

    def remove(tags: Map[String, String]) = {
      Kamon.histogram(name).remove(tags)
    }
  }

  trait RangeSampler {
    val name: String

    def increment() = Kamon.rangeSampler(name).increment()

    def increment(tags: Map[String, String]) = Kamon.rangeSampler(name).refine(tags).increment()

    def decrement() = Kamon.rangeSampler(name).decrement()

    def decrement(tags: Map[String, String]) = Kamon.rangeSampler(name).refine(tags).decrement()

    def remove() = {
      Kamon.rangeSampler(name)
    }

    def remove(tags: Map[String, String]) = {
      Kamon.rangeSampler(name).remove(tags)
    }
  }

  trait Gauge {
    val name: String

    def add() = Kamon.gauge(name)

    def add(tags: Map[String, String]) = Kamon.gauge(name).refine(tags)

    def add(value: MeasurementUnit, tags: Map[String, String]) = Kamon.gauge(name, value).refine(tags)

    def set(value: Long) = Kamon.gauge(name).set(value)

    def increment() = Kamon.gauge(name).increment()

    def increment(value: Long) = Kamon.gauge(name).increment(value)

    def decrement() = Kamon.gauge(name).decrement()

    def decrement(value: Long) = Kamon.gauge(name).decrement(value)

    def remove() = Kamon.gauge(name).remove()

    def remove(tags: Map[String, String]) = Kamon.gauge(name).remove(tags)

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
