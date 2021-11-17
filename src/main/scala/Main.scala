import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.config.{Config, ConfigFactory}
import Api.objectMapper.{getHomeById, token}
import Api.{alpha, reportHome}
import com.fasterxml.jackson.annotation.JsonFormat
import kamon.Kamon
import kamon.system.SystemMetrics

import java.io.File
import scala.io.Source
import scala.util.{Failure, Success, Try}


object Main extends App {

  startKamon(ConfigFactory.load("application.conf"))

  new alpha(){
    //run in a 10 second loop
    try {
      run()
      Thread.sleep(10000)
    }
    catch {
      case ex : Exception => println("ERROR Running - cleaning token, Exception : " + ex.toString);
    }
  }



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