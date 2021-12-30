package api.alpha

import api.common.RestBody
import api.common.Token

import java.util.Date

object AlphaObjectMapper {

  case class LoginDetails(
                           username: String,
                           password: String
                         ) extends RestBody

  case class LoginReply(
                         code: Int,
                         info: String,
                         data: Token)


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
}
