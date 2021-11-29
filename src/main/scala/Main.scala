import api.alpha.alpha
import api.tapo.{Tapo, tapoMiddleMan}
import com.typesafe.config.{Config, ConfigFactory}
import api.tapo
import com.google.gson.internal.bind.DefaultDateTypeAdapter.DateType
import kamon.Kamon
import kamon.system.SystemMetrics

import java.time.Instant
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import scala.util.{Failure, Success, Try}


object Main extends App {

  val conf1 = ConfigFactory.defaultApplication()
  val conf2 = ConfigFactory.load()

  startKamon(conf1.withFallback(conf2).resolve())

  val task = new Runnable {
  val alpha = new alpha()
  val tapo = new tapoMiddleMan(new Tapo())

    override def run(): Unit = {
      //run in a 10 second loop
      try {
        alpha.run()
        tapo.Run()
        println(Instant.now())
      }
      catch {
        case ex: Exception => println("ERROR Running - cleaning token, Exception : " + ex.toString);
      }
    }
  }

  val ex = new ScheduledThreadPoolExecutor(1)
  val f = ex.scheduleAtFixedRate(task, 1, 10, TimeUnit.SECONDS)
  //f.cancel(false)


  private def startKamon(config: Config) = {
    val system = config.getBoolean("kamon.system-metrics.jvm.enabled")
    val host = config.getBoolean("kamon.system-metrics.host.enabled")
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