import api.alpha.alpha
import api.forecast.solar.SolarForecast
import api.tapo.{Tapo, tapoMiddleMan}
import com.typesafe.config.{Config, ConfigFactory}
import kamon.Kamon

import java.time.Instant
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import scala.util.{Failure, Success, Try}


object Main extends App {

  val conf1 = ConfigFactory.defaultApplication()
  val conf2 = ConfigFactory.load()

  println("Varaibles")
  println("ALPHA_USERNAME = " + conf2.getString("alphaess.username"))
  println("ALPHA_PASSWORD = " + conf2.getString("alphaess.password"))
  println("ALPHA_SYS_SN = " + conf2.getString("alphaess.system_sn"))
  println("TAPO_USERNAME = " + conf2.getString("tapo.username"))
  println("TAPO_PASSWORD = " + conf2.getString("tapo.password"))
  println("TAPO_ADDRESSES = " + conf2.getString("tapo.addresses"))
  println("KAMON_DATADOG_API_KEY = " + conf2.getString("kamon.datadog.api.api-key"))


  startKamon(conf1.withFallback(conf2).resolve())

  val GatherRealTimeMetrics = new Runnable {
    val alpha = new alpha(conf2)
    val tapo = new tapoMiddleMan(new Tapo(),conf2)

    override def run(): Unit = {
      //run in a 10 second loop
      try {
        //alpha.run()
        //tapo.Run()
        println(Instant.now())
      }
      catch {
        case ex: Exception => println("ERROR Running - cleaning token, Exception : " + ex.toString);
      }
    }
  }

  val GatherSolarForecastMetrics = new Runnable {
    val forecast = new SolarForecast(conf2)

    override def run(): Unit = {
      //run in a 2 hour loop
      try {
        forecast.publishTodaysForcast(forecast.getTodaysForcast())
      }
      catch {
        case ex: Exception => println("ERROR: " + ex.toString);
      }
    }
  }

    val ex = new ScheduledThreadPoolExecutor(1)
    val f = ex.scheduleAtFixedRate(GatherRealTimeMetrics, 1, 10, TimeUnit.SECONDS)
    //f.cancel(false)

    val g = ex.scheduleAtFixedRate(GatherSolarForecastMetrics, 1, 120, TimeUnit.MINUTES)
    //g.cancel(false)


  private def startKamon(config: Config) = {
    // val system = config.getBoolean("kamon.system-metrics.jvm.enabled")
    // val host = config.getBoolean("kamon.system-metrics.host.enabled")
    println("Starting Kamon reporters...." + config.getStringList("kamon.reporters").toString)
    Kamon.init(config)
    Try(Kamon.reconfigure(config)) match {
      case Success(_) =>

      //TODO come back to this - why did it break coming from kamon 1.0 to 2.0 ???
      //        if (system || host) {
      //          SystemMetrics.startCollecting()
      //          println(s"Kamon system metrics are enabled[$system], ${if (system) "starting" else "not starting"}.")
      //          println(s"Kamon host metrics are enabled[$host], ${if (host) "starting" else "not starting"}.")
      //        }
      case Failure(e) =>
        println("failed", s"Failed to start Kamon with exception ${e.getLocalizedMessage}")
    }
  }
}
