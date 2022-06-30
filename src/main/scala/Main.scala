import api.alpha.AlphaObjectMapper.AlphaESSSendSetting
import api.alpha.alpha
import api.ember.ember
import api.forecast.solar.SolarForecast
import api.myenergi.myenergi_zappie
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
  val myenergi = new myenergi_zappie(conf2, reporterKamon)
  val systemControl = new api.forecast.solar.SystemControl(alpha,forecast)

  val GatherRealTimeMetrics = new Runnable {
    override def run(): Unit = {
      //run in a 10 second loop
      try {
        if (alphaEnabled) {alpha.run()}
        if (tapoEnabled) {tapo.Run()}
        if (emberEnabled) {ember.Run()}
        if (myEnergiEnabled) {myenergi.Run()}
        if(controlEnabled) {systemControl.canWeTurnOffNightCharging(alpha.getCurrentGridPull())}
        logger.info("All Metrics Gathered : " + Calendar.getInstance().getTime)
      }
      catch {
        case ex: Exception => logger.info("ERROR Running - cleaning token, Exception : " + ex.toString);
      }
    }
  }

  def PublishSolarForecastNightlySummaryMetrics()= {
      try {
        //make sure we have a full days set of data first
        if (applicationStartTime.until(Instant.now(), ChronoUnit.HOURS) > 24) {
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
        case 6  if(forecastEnabled && controlEnabled) => systemControl.EnableBatteryNightCharging()
        case 16 if(forecastEnabled) => forecast.getTomorrowForcast()
        case 23 if(forecastEnabled) => PublishSolarForecastNightlySummaryMetrics() // get most up to date metrics before we set battery charge %
        case x:Any => logger.info("current hour is '"+x+"' nothing planned to run")
      }
    }
  }

  val now = LocalDateTime.now()
  val ex = new ScheduledThreadPoolExecutor(3)
  // run Every 10 seconds
  ex.scheduleAtFixedRate(GatherRealTimeMetrics, 1, 10, TimeUnit.SECONDS)
  // run at the top of every hour
  ex.scheduleAtFixedRate(HourlyRun, Duration.between(now, now.plusHours(1).truncatedTo(ChronoUnit.HOURS)).toMillis+1000, TimeUnit.HOURS.toMillis(1), TimeUnit.MILLISECONDS)


  private def startKamon(config: Config) = {
    logger.info("Starting Kamon reporters...." + config.getStringList("kamon.reporters").toString)
    Kamon.init(config)
  }
}
