import api.alpha.AlphaObjectMapper.AlphaESSSendSetting
import api.alpha.alpha
import api.common.FileIO.jsonMapper
import api.common.Token
import api.ember.ember
import api.forecast.solar.SolarForecast
import api.myenergi.MyEnergiObjectMapper.jstatusZReply
import api.myenergi.{myenergi_eddie, myenergi_zappie}
import api.tapo.{Tapo, tapoMiddleMan}
import com.typesafe.config.{Config, ConfigFactory}
import kamon.Kamon
import metrics.KamonMetrics

import java.time.temporal.ChronoUnit
import java.time.{Duration, Instant, LocalDateTime, OffsetDateTime, ZoneOffset}
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import com.typesafe.scalalogging.LazyLogging

import java.util.Calendar


object Main extends App with LazyLogging {

  val conf1 = ConfigFactory.defaultApplication()
  val conf2 = ConfigFactory.load()

  logger.info("Variable's")
  val alphaEnabled = conf2.getBoolean("alphaess.enabled")
  val emberEnabled = conf2.getBoolean("ember.enabled")
  val tapoEnabled = conf2.getBoolean("tapo.enabled")
  val myEnergiEnabled = conf2.getBoolean("myenergi.enabled")
  val forecastEnabled = conf2.getBoolean("forecasting.enabled")
  val controlEnabled = conf2.getBoolean("alphaess.allow_control")

  logger.info("KAMON_DATADOG_API_KEY = " + conf2.getString("kamon.datadog.api.api-key"))

  if (alphaEnabled) {
    logger.info("ALPHA_USERNAME = " + conf2.getString("alphaess.username"))
    logger.info("ALPHA_PASSWORD = " + conf2.getString("alphaess.password"))
    logger.info("ALPHA_SYS_SN = " + conf2.getString("alphaess.system_sn"))
    logger.info("ALLOW_CONTROL_OF_BATTERY_CHANGE = " + conf2.getBoolean("alphaess.allow_control"))
  }
  if (emberEnabled) {
    logger.info("EMBER_USERNAME = " + conf2.getString("ember.username"))
    logger.info("EMBER_PASSWORD = " + conf2.getString("ember.password"))
  }
  if (tapoEnabled) {
    logger.info("TAPO_USERNAME = " + conf2.getString("tapo.username"))
    logger.info("TAPO_PASSWORD = " + conf2.getString("tapo.password"))
    logger.info("TAPO_ADDRESSES = " + conf2.getString("tapo.addresses"))
  }
  if (myEnergiEnabled) {
    logger.info("MYENGERI_USERNAME = " + conf2.getString("myenergi.username"))
    logger.info("MYENGERI_PASSWORD = " + conf2.getString("myenergi.password"))
  }
  if (forecastEnabled) {
    logger.info("FORECAST_LAT = " + conf2.getString("forecasting.lat"))
    logger.info("FORECAST_LON = " + conf2.getString("forecasting.lon"))
    logger.info("FORECAST_DEC = " + conf2.getString("forecasting.dec"))
    logger.info("FORECAST_AZ = " + conf2.getString("forecasting.az"))
    logger.info("FORECAST_KWH = " + conf2.getString("forecasting.kwh"))
  }

  var applicationStartTime: Instant = Instant.now()

  startKamon(conf1.withFallback(conf2).resolve())
  val reporterKamon = new KamonMetrics()

  val alpha = new alpha(conf2, reporterKamon)
  val ember = new ember(conf2, reporterKamon)
  val tapo = new tapoMiddleMan(new Tapo(), conf2, reporterKamon)
  val forecast = new SolarForecast(conf2, reporterKamon)
  val myenergi_zappi = new myenergi_zappie(conf2, reporterKamon)
  val myenergi_eddi = new myenergi_eddie(conf2, reporterKamon)
  val systemControl = new api.forecast.solar.SystemControl(alpha,forecast)

  val GatherRealTimeMetrics = new Runnable {
    override def run(): Unit = {

      //run in a 10 second loop
      try {
        if (alphaEnabled) {
          alpha.run()
        }
      }
      catch {
        case ex: Exception =>
          alpha.token = Token.empty() //it appears that the token should last 2 years , but we get a 401 after a few hours....
          logger.error("ERROR Running alphaEss - cleaning token, Exception : " + ex.printStackTrace());
      }
      try {
        if (tapoEnabled) {
          tapo.Run()
        }
      }
      catch {
        case ex: Exception => logger.error("ERROR Running tapo - cleaning token, Exception : " + ex.printStackTrace());
      }
      try {
        if (emberEnabled) {
          ember.Run()
        }
      }
      catch {
        case ex: Exception => logger.error("ERROR Running ember - cleaning token, Exception : " + ex.printStackTrace());
      }
      try {
        if (myEnergiEnabled) {
          myenergi_zappi.Run()
          myenergi_eddi.Run()
        }
      }
      catch {
        case ex: Exception => logger.error("ERROR Running myEnergi - cleaning token, Exception : " + ex.printStackTrace());
      }
      try {
        if (controlEnabled) {
          systemControl.canWeTurnOffNightCharging(alpha.getCurrentGridPull())
        }
      }
      catch {
        case ex: Exception => logger.error("ERROR Running SystemControl - cleaning token, Exception : " + ex.printStackTrace());
      }

      logger.info("All Metrics Gathered : " + Calendar.getInstance().getTime)
    }
  }

  def PublishSolarForecastNightlySummaryMetrics()= {
      try {
        //make sure we have a full days set of data first
        if (applicationStartTime.until(Instant.now(), ChronoUnit.HOURS) > 12) {
          //what was today's solar generation as a percentage of forecasted
          forecast.RunNightlySummaryMetrics(alpha.reporter.DailySolarGeneration)
          logger.info("Publish Daily forecasting Metrics")
        }
        else {
          logger.info("Didn't publish metrics, not a full days worth")
        }
        alpha.resetDailyCounter()
      }
      catch {
        case ex: Exception => logger.info("ERROR: " + ex.toString);
      }
  }

  val HourlyRun = new Runnable {
    override def run(): Unit = {
      logger.info("running hourly Check")
      Calendar.getInstance().get(Calendar.HOUR_OF_DAY) match
      {
        //what do we want to run at what hour
        case 1  if(forecastEnabled && controlEnabled) => systemControl.setSystemSettingsBasedOnGeneratedForecast()
        case 2  => Handle2amCalls()
        case 5  => Handle5amCalls()
        case 16 if(forecastEnabled) => forecast.getTomorrowForcast()
        case 23 if(forecastEnabled) => PublishSolarForecastNightlySummaryMetrics() // get most up to date metrics before we set battery charge %
        case x:Any => logger.info("current hour is '"+x+"' nothing planned to run")
      }
    }
  }

  def Handle2amCalls(): Unit =
  {
    if(myEnergiEnabled) {
      myenergi_zappi.DoNightBoost(21,"0500")
      myenergi_eddi.SetNormalMode()
    }
  }

  def Handle5amCalls(): Unit =
  {
    if(forecastEnabled && controlEnabled) {
      systemControl.EnableBatteryNightCharging()
    }
    if(myEnergiEnabled) {
      myenergi_zappi.SetStopMode()
      myenergi_eddi.SetStopMode()
    }
  }

  val now = LocalDateTime.now()
  val TenSecondCycle = new ScheduledThreadPoolExecutor(3)
  val OneHourCycle = new ScheduledThreadPoolExecutor(3)
  // run Every 10 seconds
  TenSecondCycle.scheduleAtFixedRate(GatherRealTimeMetrics, 1, 10, TimeUnit.SECONDS)
  // run at the top of every hour
  OneHourCycle.scheduleAtFixedRate(HourlyRun, Duration.between(now, now.plusHours(1).truncatedTo(ChronoUnit.HOURS)).toMillis+1000, TimeUnit.HOURS.toMillis(1), TimeUnit.MILLISECONDS)


  private def startKamon(config: Config) = {
    logger.info("Starting Kamon reporters...." + config.getStringList("kamon.reporters").toString)
   Kamon.init(config)
  }
}
