package api.forecast.solar

import api.forecast.solar.ForcastingModel.SolarForcastingReply
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.config.Config
import metrics.KamonMetrics
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils

class SolarForecast(config : Config, reporterKamon : KamonMetrics) {

  val lat = config.getString("forcasting.lat")
  val lon = config.getString("forcasting.lon")
  val dec = config.getString("forcasting.dec")
  val az = config.getString("forcasting.az")
  val kwh = config.getString("forcasting.kwh")

  val jsonMapper = new ObjectMapper()
  jsonMapper.registerModule(DefaultScalaModule)
  jsonMapper.registerModule(new JavaTimeModule())
  jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  private var todaysForecast = 0

  def getTodaysForcast():Integer ={
      // create an HttpGet object
      val uri = "https://api.forecast.solar/estimate/watthours/day/"+lat+"/"+lon+"/"+dec+"/"+az+"/"+kwh
      val get = new HttpGet(uri)

      // set the Content-type
      get.setHeader("Content-type", "application/json")
      get.setHeader("Host", "api.forecast.solar")
      // send the get request
      val response = (new DefaultHttpClient).execute(get)
      // print the response headers
      val reply = EntityUtils.toString(response.getEntity, "UTF-8")

      //due to the bad json format received from the service (dynamic name values), mapping to too much work for now
      todaysForecast = jsonMapper.readTree(reply).get("result").elements().next().asInt()
      println("Forecaster Generation for today is :"+todaysForecast+" watts")
    todaysForecast
    }

  def publishTodaysForcast(value: Integer) = {
    reporterKamon.forecasting_todaysGeneration.set(value.longValue())
  }

  def RunNightlySummaryMetrics(totalSolarGeneration: Long): Unit =
  {
    val  accuracy = (todaysForecast/totalSolarGeneration)*100
    reporterKamon.forecasting_todaysAccuracy.set(accuracy)

    //reset the value
    todaysForecast = 0
  }

}

