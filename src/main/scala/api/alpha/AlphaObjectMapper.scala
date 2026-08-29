package api.alpha

import api.common.RestBody
import api.common.Token
import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonInclude}

import java.util.Date
import scala.util.Try

object AlphaObjectMapper {


  case class LoginReply(
                         accessToken: String,
                         refreshToken: String,
                         idToken: String,
                         tokenType: String,
                         scope: String,
                         sessionState: String,
                         notBeforePolicy: Long,
                         expiresIn: Long,
                         refreshExpiresIn: Long,
                         pilot: Boolean
                       )

  case class UpdateFITReply(
                             code: Int,
                             msg: String,
                             expMsg: String,
                             data: Boolean,
                             extra: Option[String])


  @JsonIgnoreProperties(ignoreUnknown = true)
  case class SystemDetailsReply(
                                 status: String,
                                 power: PowerMetrics,
                                 energy: EnergyMetrics,
                                 income: IncomeMetrics,
                                 sameDayEnergyAvailable: Boolean,
                                 batteryHeating: BatteryHeating,
                                 batteryHeatingList: List[BatteryHeatingEntry]
                               ) extends RestBody


  @JsonIgnoreProperties(ignoreUnknown = true)
  case class PowerMetrics(
                           solar: Double,
                           battery: Double,
                           grid: Double,
                           diesel: Double,
                           soc: Double,
                           load: Double,
                           dispatching: Boolean,
                           pv1: Double,
                           pv2: Double,
                           pv3: Double,
                           pv4: Double,
                           pvAc: Double,
                           observedAt: String,
                           upsMode: Int,
                           isDispatch: Boolean,
                           isGridOff: Boolean
                         )


  @JsonIgnoreProperties(ignoreUnknown = true)
  case class EnergyMetrics(
                            generate: Double,
                            consumption: Double
                          )


  @JsonIgnoreProperties(ignoreUnknown = true)
  case class IncomeMetrics(
                            value: Double
                          )


  @JsonIgnoreProperties(ignoreUnknown = true)
  case class BatteryHeating()


  @JsonIgnoreProperties(ignoreUnknown = true)
  case class BatteryHeatingEntry(
                                  sysSn: String
                                )


  @JsonIgnoreProperties(ignoreUnknown = true)
  case class AlphaESSFeedStrategyList(
                                       feedInControl: AlphaEssFeedInControl
                                     )

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class AlphaEssFeedInControl(
                                    batteryReserveSoc: Double,
                                    batteryFeedCutoffSoc: Double,
                                    enabled: Boolean,
                                    feedStrategy: List[FeedStrategyVO],
                                    vppSign: String,
                                    preChargeEn: Boolean,
                                    feedInPowerLimit: Double
                                  )

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class FeedStrategyVO(
                             startTime: String,
                             endTime: String,
                             feedPower: Double
                           )


  @JsonIgnoreProperties(ignoreUnknown = true)
  final case class AlphaESSCycleStrategy(
                                          `type`: String,
                                          residential: CycleData
                                        )

  @JsonIgnoreProperties(ignoreUnknown = true)
  final case class CycleData(
                              batteryReserve: Int,
                              strategy: String,
                              customMode: CustomMode,
                              aiMode: AiMode
                            )

  @JsonIgnoreProperties(ignoreUnknown = true)
  final case class CustomMode(
                               rangeList: List[StrategyRange],
                               chargeEnable: Boolean,
                               dischargeEnable: Boolean,
                               version: String,
                               chargeCutOffSoc: Double
                             )

  @JsonIgnoreProperties(ignoreUnknown = true)
  final case class StrategyRange(
                                  daysOfWeek: List[Int],
                                  repeat: String,
                                  startTime: String,
                                  endTime: String,
                                  strategy: String,
                                  chargePower: Double,
                                  chargeCutOffSoc: Double
                                )

  @JsonIgnoreProperties(ignoreUnknown = true)
  final case class AiMode(
                           beta: Boolean,
                           inWhitelist: Boolean,
                           strategyProfile: String
                         )

  @JsonIgnoreProperties(ignoreUnknown = true)
  final case class CycleDataUpdate(
                                    id: String,
                                    batteryReserve: Int,
                                    strategy: String,
                                    rangeList: List[StrategyRange],
                                    chargeEnable: Boolean,
                                    dischargeEnable: Boolean,
                                    version: String,
                                    chargeCutOffSoc: Double
                                  )

  object AlphaESSUpdateChargeCycleInfo {

    def from(receivedType: CycleData, id: String): CycleDataUpdate = {
      CycleDataUpdate(
        id = id,
        batteryReserve = receivedType.batteryReserve,
        strategy = receivedType.strategy,
        rangeList = receivedType.customMode.rangeList,
        chargeEnable = receivedType.customMode.chargeEnable,
        dischargeEnable = receivedType.customMode.dischargeEnable,
        version = receivedType.customMode.version,
        chargeCutOffSoc = receivedType.customMode.chargeCutOffSoc
      )
    }
  }
}