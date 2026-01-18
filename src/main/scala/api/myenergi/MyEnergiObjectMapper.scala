package api.myenergi

import api.common.RestBody
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

object MyEnergiObjectMapper {

  case class DigestReplyDetails(
                                 status: Int,
                                 statustext: String,
                                 asn: String,
                                 fwv: String
                               ) extends RestBody

  case class jstatusZReply(
                            zappi: Array[ZappiMapper]
                          )

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class ZappiMapper(
                          sno: Int,
                          dat: String,
                          tim: String,
                          ectp1: Int,
                          ectp2: Int,
                          ectp3: Int,
                          ectt1: String,
                          ectt2: String,
                          ectt3: String,
                          bsm: Int,
                          bst: Int,
                          cmt: Int,
                          dst: Int,
                          div: Int,
                          frq: Double,
                          fwv: String,
                          grd: Int,
                          pha: Int,
                          pri: Int,
                          sta: Int,
                          tz: Int,
                          vol: Int,
                          che: Double,
                          bss: Int,
                          lck: Int,
                          pst: String,
                          tbk: Int,
                          zmo: Int,
                          zs: Int,
                          ectp4: Int,
                          ectt4: String,
                          ectt5: String,
                          ectt6: String,
                          mgl: Int,
                          sbh: Int,
                          sbk: Int)

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class HarviMapper(
                          deviceClass: String,
                          sno: Int,
                          dat: String,
                          tim: String,
                          ectp1: Int,
                          ectp2: Int,
                          ectp3: Int,
                          ectt1: String,
                          ectt2: String,
                          ectt3: String,
                          ect1p: Int,
                          ect2p: Int,
                          ect3p: Int,
                          fwv: String,
                          productCode: String
                        )

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class EddiMapper(
                         sno: Int,
                         dat: String,
                         tim: String,
                         ectt1: String,
                         ectt2: String,
                         ectt3: String,
                         bsm: Int,
                         bst: Int,
                         cmt: Int,
                         dst: Int,
                         div: Int,
                         frq: Double,
                         fwv: String,
                         pha: Int,
                         pri: Int,
                         sta: Int,
                         tz: Int,
                         vol: Int,
                         hpri: Int,
                         hno: Int,
                         ht1: String,
                         ht2: String,
                         r1a: Int,
                         r2a: Int,
                         r1b: Int,
                         r2b: Int,
                         rbc: Int,
                         tp1: Int,
                         tp2: Int)
  @JsonIgnoreProperties(ignoreUnknown = true)
  case class LibbiMapper(
                          deviceClass: Option[String] = None
                         )

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class MyEnergiEntry(
                              eddi: Option[List[EddiMapper]] = None,
                              harvi: Option[List[HarviMapper]] = None,
                              libbi: Option[List[LibbiMapper]] = None,
                              zappi: Option[List[ZappiMapper]] = None,

                              // meta object (the last element in the array)
                              asn: Option[String] = None,
                              fwv: Option[String] = None,
                              vhub: Option[Int] = None
                            )

  case class jstatusEReply(
                            eddi: Array[EddiMapper]
                          )

}