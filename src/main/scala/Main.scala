import api.alpha.alpha
import api.ember.ember
import api.forecast.solar.SolarForecast
import api.tapo.{Tapo, tapoMiddleMan}
import com.typesafe.config.{Config, ConfigFactory}
import kamon.Kamon
import metrics.KamonMetrics

import java.time.temporal.ChronoUnit
import java.time.{Duration, Instant, LocalDateTime, OffsetDateTime, ZoneOffset, temporal}
import java.util.{Date, concurrent}
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import scala.util.{Failure, Success, Try}


object Main extends App {

  val conf1 = ConfigFactory.defaultApplication()
  val conf2 = ConfigFactory.load()

  println("Variable's")
  println("ALPHA_USERNAME = " + conf2.getString("alphaess.username"))
  println("ALPHA_PASSWORD = " + conf2.getString("alphaess.password"))
  println("ALPHA_SYS_SN = " + conf2.getString("alphaess.system_sn"))
  println("EMBER_USERNAME = " + conf2.getString("ember.username"))
  println("EMBER_PASSWORD = " + conf2.getString("ember.password"))
  println("TAPO_USERNAME = " + conf2.getString("tapo.username"))
  println("TAPO_PASSWORD = " + conf2.getString("tapo.password"))
  println("TAPO_ADDRESSES = " + conf2.getString("tapo.addresses"))
  println("KAMON_DATADOG_API_KEY = " + conf2.getString("kamon.datadog.api.api-key"))
  println("FORCAST_LAT = " + conf2.getString("forcasting.lat"))
  println("FORCAST_LON = " + conf2.getString("forcasting.lon"))
  println("FORCAST_DEC = " + conf2.getString("forcasting.dec"))
  println("FORCAST_AZ = " + conf2.getString("forcasting.az"))
  println("FORCAST_KWH = " + conf2.getString("forcasting.kwh"))

  var applicationStartTime : Instant = Instant.now()

  startKamon(conf1.withFallback(conf2).resolve())
  val reporterKamon = new KamonMetrics()

  val alpha = new alpha(conf2,reporterKamon)
  val ember = new ember(conf2,reporterKamon)
  val tapo = new tapoMiddleMan(new Tapo(), conf2,reporterKamon)
  val forecast = new SolarForecast(conf2,reporterKamon)

  val GatherRealTimeMetrics = new Runnable {
    override def run(): Unit = {
      //run in a 10 second loop
      try {
        alpha.run()
        tapo.Run()
        ember.Run()
        println(Instant.now())
      }
      catch {
        case ex: Exception => println("ERROR Running - cleaning token, Exception : " + ex.toString);
      }
    }
  }

  //runs at 1 am each day - get forecast
  val GatherSolarForecastMetrics = new Runnable {
    override def run(): Unit = {

      try {
        //what did we predict today's solar generation will be
        forecast.publishTodaysForcast(forecast.getTodaysForcast())
        println("Publish forecasting Metrics")
      }
      catch {
        case ex: Exception => println("ERROR: " + ex.toString);
      }
    }
  }

  //run at 11pm every day - publish forecast accuracy
  val PublishSolarForecastNightlySummaryMetrics = new Runnable {
    override def run(): Unit = {
      try {
        //make sure we have a full days set of data first

        if(applicationStartTime.until(Instant.now(),ChronoUnit.HOURS) > 24) {
          //what was today's solar generation as a percentage of forecasted
          forecast.RunNightlySummaryMetrics(alpha.reporter.DailySolarGeneration)
          println("Publish Daily forecasting Metrics")
        }
        else
          println("Didn't publish metrics, not a full days worth")

        alpha.resetDailyCounter()
      }
      catch {
        case ex: Exception => println("ERROR: " + ex.toString);
      }
    }
  }

  val now = OffsetDateTime.now( ZoneOffset.UTC )

  val ex = new ScheduledThreadPoolExecutor(1)
  ex.scheduleAtFixedRate(GatherRealTimeMetrics, 1, 10, TimeUnit.SECONDS)

  //Run at 1am Each day
  ex.scheduleAtFixedRate(GatherSolarForecastMetrics, Duration.between(
    now,
    now.toLocalDate()
      .plusDays( 1 )
      .atStartOfDay( ZoneOffset.UTC )
      .plusHours(1)
  ).toMillis,
    TimeUnit.DAYS.toMillis( 1 ),
    TimeUnit.MILLISECONDS
  )

  //Run at 11pm Each day
  ex.scheduleAtFixedRate(PublishSolarForecastNightlySummaryMetrics, Duration.between(
      now,
      now.toLocalDate()
        .plusDays( 1 )
        .atStartOfDay( ZoneOffset.UTC )
        .minusHours(1)
    ).toMillis,
    TimeUnit.DAYS.toMillis( 1 ),
    TimeUnit.MILLISECONDS
  )


  private def startKamon(config: Config) = {
    println("Starting Kamon reporters...." + config.getStringList("kamon.reporters").toString)
    Kamon.init(config)
  }
}
