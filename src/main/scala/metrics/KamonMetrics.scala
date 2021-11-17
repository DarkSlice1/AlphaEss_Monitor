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
}
