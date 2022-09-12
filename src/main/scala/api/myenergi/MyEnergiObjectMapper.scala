package api.myenergi

import api.common.RestBody

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

  case class jstatusEReply(
                            eddi: Array[EddiMapper]
                          )

case class EddiMapper(
                        sno: Int,
                        dat:String,
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
}
