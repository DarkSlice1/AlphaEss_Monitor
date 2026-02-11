package api.alpha

import api.common.RestBody
import api.common.Token
import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonInclude}

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

  case class UpdateFITReply(
                         code: Int,
                         msg: String,
                         expMsg: String,
                         data: Boolean,
                         extra:Option[String])


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

  case class AlphaESSFeedStrategyList(
                                       code: Int,
                                       msg: String,
                                       expMsg : String,
                                       data: AlphaEssFeedStrategyData,
                                       extra: Option[String]
                                     )

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class AlphaEssFeedStrategyConfig(
                                       batteryEn: Int,
                                       batteryFeedCutoffSoc: BigDecimal,              // 15 -> 15
                                       id: String,                                    // "zuUVCm..."
                                       feedStrategyDTOList: List[FeedStrategyVO],
                                       prechargeEn: Int
                                     )

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class AlphaEssFeedStrategyData(
                                     batteryEn: Int,
                                     batteryFeedCutoffSoc: BigDecimal,
                                     poinv: BigDecimal,
                                     timePeriodLimit: Int,
                                     batUseCap: BigDecimal,
                                     feedStrategyVOList: List[FeedStrategyVO],
                                     prechargeEn: Int,
                                     prechargeSoc: Option[BigDecimal],     // null -> None
                                     feedInAlertContent: String,
                                     strategyStatus: String
                                   )

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class FeedStrategyVO(
                                   id: Long,
                                   sysSn: String,
                                   start: String,   // "HH:mm"
                                   end: String,     // "HH:mm"
                                   feedPower: Int,
                                   sort: Int
                           )

  object FeedStrategyVO {
    def from(receivedType: FeedStrategyVO): FeedStrategyVO = {
      new FeedStrategyVO(
        id = receivedType.id,
        sysSn = receivedType.sysSn,
        start = receivedType.start, // "HH:mm"
        end = receivedType.end, // "HH:mm"
        feedPower = receivedType.feedPower,
        sort = receivedType.sort
      )
    }
  }
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

  object AlphaESSUpdateChargeCycleInfo {
    def from(receivedType: CycleData, id:String): CycleDataUpdate = {
      new CycleDataUpdate(
        executeCycleType = receivedType.executeCycleType,
        gridChargeCycle  = receivedType.gridChargeCycle,
        ctrDisCycle      = receivedType.ctrDisCycle,
        batUseCap = receivedType.batUseCap.toInt,
        upsReserve = receivedType.upsReserve,
        loadcutoutEn = receivedType.loadcutoutEn,
        cutoffSoc = receivedType.cutoffSoc,
        wakeupSoc = receivedType.wakeupSoc,
        isSupportDischargeSoc = receivedType.isSupportDischargeSoc,
        isSupportChargerPower = receivedType.isSupportChargerPower,
        loadcutoutEn2 = receivedType.loadcutoutEn2,
        cutoffSoc2 = receivedType.cutoffSoc2,
        wakeupSoc2 = receivedType.wakeupSoc2,
        poinv = receivedType.poinv,
        isMicroStorageTwoGen = receivedType.isMicroStorageTwoGen,
        chargeTimeList = receivedType.dayChargeTimeList,
        dischargeTimeList = receivedType.dayDischargeTimeList,
        id = id
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


@JsonIgnoreProperties(ignoreUnknown = true)
final case class AlphaESSCycleStrategy(
                              code: Int,
                              msg: String,
                              expMsg: Option[String],      // null -> None
                              data: CycleData,
                              extra: Option[String]        // null -> None (could be JsonNode if it's not always a string)
                            )

@JsonIgnoreProperties(ignoreUnknown = true)
final case class CycleData(
                            executeCycleType: Int,
                            gridChargeCycle: Int,
                            ctrDisCycle: Int,
                            weekChargeTimeList: Option[List[TimeWindow]] = None,     // null in sample
                            weekDischargeTimeList: Option[List[TimeWindow]] = None,  // null in sample
                            dayChargeTimeList: List[TimeWindow],
                            dayDischargeTimeList: List[TimeWindow],
                            batUseCap: BigDecimal,
                            upsReserveEnable: Boolean,
                            upsReserve: Int,
                            batCapRange: List[Int],          // e.g. [4, 100]
                            loadcutoutEn: Int,
                            cutoffSoc: Int,
                            wakeupSoc: Int,
                            loadcutoutEn2: Int,
                            cutoffSoc2: Int,
                            wakeupSoc2: Int,
                            isSupportDischargeSoc: Boolean,
                            isSupportChargerPower: Boolean,
                            poinv: Int,
                            isSiteDevice: Option[Boolean],   // null -> None
                            isSupportOffGridSocControl: Boolean,
                            isSupportOffGridSocControl2: Boolean,
                            onGridPower: Int,
                            totalPoinv: Int,
                            isMicroStorageTwoGen: Boolean
                          )

@JsonIgnoreProperties(ignoreUnknown = true)
final case class CycleDataUpdate(
                                  id: String,
                                  batUseCap: BigInt,
                                  upsReserve: Int,
                                  executeCycleType: Int,
                                  gridChargeCycle: Int,
                                  ctrDisCycle: Int,
                                  loadcutoutEn: Int,
                                  cutoffSoc: Int,
                                  wakeupSoc: Int,
                                  loadcutoutEn2: Int,
                                  cutoffSoc2: Int,
                                  wakeupSoc2: Int,
                                  chargeTimeList: List[TimeWindow],
                                  dischargeTimeList: List[TimeWindow],
                                  isSupportDischargeSoc: Boolean,
                                  isSupportChargerPower: Boolean,
                                  isMicroStorageTwoGen: Boolean,
                                  poinv: Int
                                )

@JsonIgnoreProperties(ignoreUnknown = true)
final case class TimeWindow(
                             chargeLimit: Int,     // 95.00, 0.00
                             beginTime: String,           // "HH:mm"
                             endTime: String,             // "HH:mm"
                             sort: Int,
                             chargePower: Int,
                             weeks: List[Int],            // [7,1,2,3,4,5,6]
                             feedMode: Int,
                             equipGroupId: Int,
                             feedPower: Int
                           )