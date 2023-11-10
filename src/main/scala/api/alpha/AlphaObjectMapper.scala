package api.alpha

import api.common.RestBody
import api.common.Token
import com.fasterxml.jackson.annotation.JsonInclude

import java.util.Date
import scala.util.Try

object AlphaObjectMapper {

  case class LoginDetails(
                           username: String,
                           password: String
                         ) extends RestBody

  case class GetMetricsDetails(
                          sys_sn: String,
                          noLoading: Boolean
                         ) extends RestBody

  case class LoginReply(
                         code: Int,
                         msg: String,
                         expMsg: String,
                         data: Token)


  case class SystemDetails(
                            sys_sn: String,
                            noLoading: String
                          ) extends RestBody

  case class SystemDetailsReply(
                                 code: Int,
                                 msg: String,
                                 expMsg: String,
                                 data: AlphaMetrics
                               ) extends RestBody

  case class AlphaMetrics
  (
  ppv: Double,
  soc: Double,
  pev: Int,
  ppvSlave: Int,
  upsModel: Int,
  hasChargingPile: String,
  hasSecData: Boolean,
  pload: Double,
  pgrid: Double,
  pbat: Double
  )

  case class AlphaESSGetCustomMenuEssList(
                                           code: Int,
                                           msg: String,
                                           expMsg : String,
                                           data: Array[AlphaESSGetCustomMenuEssListData]
                                         )
  case class AlphaESSGetCustomMenuEssListData(
                                              systemId: String,
                                              sysSn: String,
                                              sysName: String,
                                              popv: Double,
                                              minv: String,
                                              poinv: Double,
                                              cobat: Double,
                                              mbat: String,
                                              surpluscobat: Double,
                                              uscapacity: Double,
                                              emsStatus: String,
                                              transFrequency: Int,
                                              parallelEn: Int,
                                              parallelMode: Int,
                                              remark: String
                                             )


  case class AlphaESSReceivedSetting(
                                      code: Int,
                                      msg: String,
                                      expMsg : String,
                                      data: AlphaESSChargeConfigInfo
                                    )


  case class AlphaESSChargeConfigInfo(
                                          id: String,
                                          basicModeJp: String,//null
                                          peaceModeJp: String,//null
                                          vppModeJp: String,//null
                                          gridCharge: Int,
                                          timeChaf1: String,
                                          timeChae1: String,
                                          timeChaf2: String,
                                          timeChae2: String,
                                          ctrDis: Int,
                                          timeDisf1: String,
                                          timeDise1: String,
                                          timeDisf2: String,
                                          timeDise2: String,
                                          batHighCap: Double,
                                          batUseCap: Double,
                                          batCapRange: Array[Int],
                                          isJapaneseDevice: Boolean,
                                          upsReserveEnable: Boolean,
                                          upsReserve: Int,
                                          mbat: String
                                        )
  object AlphaESSUpdateChargeConfigInfo{
    def from(receivedType: AlphaESSChargeConfigInfo) : AlphaESSUpdateChargeConfigInfo= {
      new AlphaESSUpdateChargeConfigInfo(
        id = receivedType.id,
        basicModeJp = receivedType.basicModeJp,
        peaceModeJp = receivedType.peaceModeJp,
        vppModeJp = receivedType.vppModeJp,
        gridCharge = receivedType.gridCharge,
        timeChaf1 = receivedType.timeChaf1,
        timeChae1 = receivedType.timeChae1,
        timeChaf2 = receivedType.timeChaf2,
        timeChae2 = receivedType.timeChae2,
        ctrDis = receivedType.ctrDis,
        timeDisf1 = receivedType.timeDisf1,
        timeDise1 = receivedType.timeDise1,
        timeDisf2 = receivedType.timeDisf2,
        timeDise2 = receivedType.timeDise2,
        batHighCap = receivedType.batHighCap,
        batUseCap = receivedType.batUseCap,
        batCapRange = receivedType.batCapRange,
        isJapaneseDevice = receivedType.isJapaneseDevice,
        upsReserveEnable = receivedType.upsReserveEnable,
        upsReserve = receivedType.upsReserve,
        mbat = receivedType.mbat
      )
    }
  }

  case class AlphaESSUpdateChargeConfigInfo(
                                    id: String,
                                    basicModeJp: String,//null
                                    peaceModeJp: String,//null
                                    vppModeJp: String,//null
                                    gridCharge: Int,
                                    timeChaf1: String,
                                    timeChae1: String,
                                    timeChaf2: String,
                                    timeChae2: String,
                                    ctrDis: Int,
                                    timeDisf1: String,
                                    timeDise1: String,
                                    timeDisf2: String,
                                    timeDise2: String,
                                    batHighCap: Double,
                                    batUseCap: Double,
                                    batCapRange: Array[Int],
                                    isJapaneseDevice: Boolean,
                                    upsReserveEnable: Boolean,
                                    upsReserve: Int,
                                    mbat: String
                                ) extends RestBody

}
