package metrics

import kamon.Kamon
import kamon.datadog.DatadogAgentReporter

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


  object CurrentTemp extends Histogram {
    val name = "CurrentTemp"
  }

  object TargetTemp extends Histogram {
    val name = "TargetTemp"
  }

  object BurnActive extends Histogram {
    val name = "BurnActive"
  }

  object IsOnline extends Histogram {
    val name = "IsOnline"
  }

  object ppv1 extends Histogram {
    val name = "ppv1"
  }

  object ppv2 extends Histogram {
    val name = "ppv2"
  }

  object ppv3 extends Histogram {
    val name = "ppv3"
  }

  object ppv4 extends Histogram {
    val name = "ppv4"
  }

  object preal_l1 extends Histogram {
    val name = "preal_l1"
  }

  object preal_l2 extends Histogram {
    val name = "preal_l2"
  }

  object preal_l3 extends Histogram {
    val name = "preal_l3"
  }

  object pmeter_l1 extends Histogram {
    val name = "pmeter_l1"
  }

  object pmeter_l2 extends Histogram {
    val name = "pmeter_l2"
  }

  object pmeter_l3 extends Histogram {
    val name = "pmeter_l3"
  }

  object pmeter_dc extends Histogram {
    val name = "pmeter_dc"
  }

  object soc extends Histogram {
    val name = "soc"
  }

  object factory_flag extends Histogram {
    val name = "factory_flag"
  }

  object pbat extends Histogram {
    val name = "pbat"
  }

  object sva extends Histogram {
    val name = "sva"
  }

  object varac extends Histogram {
    val name = "varac"
  }

  object vardc extends Histogram {
    val name = "vardc"
  }

  object ev1_power extends Histogram {
    val name = "ev1_power"
  }

  object ev1_chgenergy_real extends Histogram {
    val name = "ev1_chgenergy_real"
  }

  object ev1_mode extends Histogram {
    val name = "ev1_mode"
  }

  object ev2_power extends Histogram {
    val name = "ev2_power"
  }

  object ev2_chgenergy_real extends Histogram {
    val name = "ev2_chgenergy_real"
  }

  object ev2_mode extends Histogram {
    val name = "ev2_mode"
  }

  object ev3_power extends Histogram {
    val name = "ev3_power"
  }

  object ev3_chgenergy_real extends Histogram {
    val name = "ev3_chgenergy_real"
  }

  object ev3_mode extends Histogram {
    val name = "ev3_mode"
  }

  object ev4_power extends Histogram {
    val name = "ev4_power"
  }

  object ev4_chgenergy_real extends Histogram {
    val name = "ev4_chgenergy_real"
  }

  object ev4_mode extends Histogram {
    val name = "ev4_mode"
  }

  object poc_meter_l1 extends Histogram {
    val name = "poc_meter_l1"
  }

  object poc_meter_l2 extends Histogram {
    val name = "poc_meter_l2"
  }

  object poc_meter_l3 extends Histogram {
    val name = "poc_meter_l3"
  }


}
