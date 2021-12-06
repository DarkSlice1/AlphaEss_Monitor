package api.forecast.solar

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

object ForcastingModel {


  case class SolarForcastingReply(result : result, message :message)

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class result(today:String, todayValue:Int,tomorrow:String, tomorrowValue:Int)
  case class message(code: Int,`type`:String,text:String,info:info, ratelimit:ratelimit)
  case class info(latitude:Double,longitude:Double,place :String,timezone:String)
  case class ratelimit(period:Int,limit:Int,remaining:Int)
}


//{
//  "result": {
//    "2021-12-06": 3310,
//    "2021-12-07": 3004
//  },
//
//  "message": {
//    "code": 0,
//    "type": "success",
//    "text": "",
//    "info":
//     {
//      "latitude": 53.338,
//      "longitude": -8.9456,
//      "place": "H91 Galway, Connacht, IE",
//      "timezone": "Europe/Dublin"
//    },
//    "ratelimit": {
//      "period": 3600,
//      "limit": 12,
//      "remaining": 11
//    }
//  }
//}