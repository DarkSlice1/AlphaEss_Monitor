package api.alpha

import java.time.Instant
import java.util.Date

object AlphaObjectMapper {

  //{"code":200,"info":"Success","data":{"_id":"61954899033b4c578ca1cb89","createtime":"2021-11-17 18:23:21","uploadtime":"2021-11-17 18:22:42","sn":"AL2002321060150","ppv1":0.0,"ppv2":0.0,"ppv3":0.0,"ppv4":0.0,"preal_l1":0.0,"preal_l2":0.0,"preal_l3":0.0,"pmeter_l1":858.0,"pmeter_l2":0.0,"pmeter_l3":0.0,"pmeter_dc":0.0,"soc":11.6,"factory_flag":0,"pbat":0.0000,"sva":0.0,"varac":61.0,"vardc":0.0,"ev1_power":0,"ev1_chgenergy_real":0.0,"ev1_mode":0,"ev2_power":0,"ev2_chgenergy_real":0.0,"ev2_mode":0,"ev3_power":0,"ev3_chgenergy_real":0.0,"ev3_mode":0,"ev4_power":0,"ev4_chgenergy_real":0.0,"ev4_mode":0,"poc_meter_l1":0.0,"poc_meter_l2":0.0,"poc_meter_l3":0.0}}

  class RestBody()

  case class LoginDetails(
                           username: String,
                           password: String
                         ) extends RestBody

  case class LoginReply(
                         code: Int,
                         info: String,
                         data: token)

  case class token(
                    AccessToken: String,
                    ExpiresIn: Double,
                    TokenCreateTime: Date,
                    RefreshTokenKey: String)

  case class SystemDetails(
                            sys_sn: String,
                            noLoading: String
                          ) extends RestBody

  case class SystemDetailsReply(
                                 code: Int,
                                 info: String,
                                 data: AlphaMetrics
                               ) extends RestBody

  case class AlphaMetrics
  (
    _id: String,
    createtime: Date,
    uploadtime: Date,
    sn: String,
    ppv1: Double,
    ppv2: Double,
    ppv3: Double,
    ppv4: Double,
    preal_l1: Double,
    preal_l2: Double,
    preal_l3: Double,
    pmeter_l1: Double,
    pmeter_l2: Double,
    pmeter_l3: Double,
    pmeter_dc: Double,
    soc: Double,
    factory_flag: Int,
    pbat: Double,
    sva: Double,
    varac: Double,
    vardc: Double,
    ev1_power: Int,
    ev1_chgenergy_real: Double,
    ev1_mode: Int,
    ev2_power: Int,
    ev2_chgenergy_real: Double,
    ev2_mode: Int,
    ev3_power: Int,
    ev3_chgenergy_real: Double,
    ev3_mode: Int,
    ev4_power: Int,
    ev4_chgenergy_real: Double,
    ev4_mode: Int,
    poc_meter_l1: Double,
    poc_meter_l2: Double,
    poc_meter_l3: Double
  )


  object token {
    def empty(): token = {
      new token("", 0, Date.from(Instant.now()), "")
    }
  }

  object RestBody {
    def empty(): RestBody = {
      new RestBody()
    }
  }
}
