import com.typesafe.config.{Config, ConfigFactory}
import Api.alpha
import kamon.Kamon
import kamon.system.SystemMetrics

import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import scala.util.{Failure, Success, Try}


object Main extends App {

  val conf1 = ConfigFactory.defaultApplication()
  val conf2 = ConfigFactory.load()

  startKamon(conf1.withFallback(conf2).resolve())

  val task = new Runnable {
  val app = new alpha()

    override def run(): Unit = {
      //run in a 10 second loop
      try {
        app.run()
        //Thread.sleep(10000)
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
    Try(Kamon.reconfigure(config)) match {
      case Success(_) =>
        Kamon.loadReportersFromConfig()
        if (system || host) {
          SystemMetrics.startCollecting()
          println(s"Kamon system metrics are enabled[$system], ${if (system) "starting" else "not starting"}.")
          println(s"Kamon host metrics are enabled[$host], ${if (host) "starting" else "not starting"}.")
        }
      case Failure(e) =>
        println("failed", s"Failed to start Kamon with exception ${e.getLocalizedMessage}")
    }
  }
}