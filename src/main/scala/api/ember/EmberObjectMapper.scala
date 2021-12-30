package api.ember


import api.common.RestBody

object EmberObjectMapper {

  case class GatewayID (gateWayId:String) extends RestBody

  case class LoginDetails(
                           password: String,
                           model: String = "iPhone XS",
                           os: String = "13.5",
                           `type`: Int = 2,
                           appVersion: String = "2.0.4",
                           userName: String
                         ) extends RestBody

  case class LoginReply(
                         data: token,
                         message: String,
                         status: Int,
                         timestamp: Long)

  case class token(
                    refresh_token: String,
                    token: String)

  case class GatewayReply(
                           data: Array[GatewayReplyData],
                           message: String,
                           status: Int,
                           timestamp: Long
                         )

  case class GatewayReplyData(
                               deviceType: Int,
                               gatewayid: String,
                               invitecode: String,
                               name: String,
                               productId: String,
                               sysTemType: String,
                               uid: String,
                               zoneCount: Int)

  case class HomeMetrics(data: Array[HomeMetricsData],
                         message: String,
                         status: Int,
                         timestamp: Long)

  case class HomeMetricsData(
                              deviceDays: Array[HomeMetricsDeviceData],
                              deviceType: Int,
                              isonline: Boolean,
                              mac: String,
                              name: String,
                              pointDataList: Array[HomeMetricsDataList],
                              productId: String,
                              systemType: String,
                              uid: String,
                              zoneid: String
                            )


  case class HomeMetricsDeviceData(
                                    dayType: Int,
                                    deviceId: String,
                                    id: String,
                                    p1: HomeMetricsDeviceDataP,
                                    p2: HomeMetricsDeviceDataP,
                                    p3: HomeMetricsDeviceDataP
                                  )

  case class HomeMetricsDeviceDataP(
                                     createTime: String,
                                     delFlag: Int,
                                     endTime: Int,
                                     id: String,
                                     startTime: Int,
                                     updateTime: String
                                   )

  case class HomeMetricsDataList(
                                  pointIndex: Int,
                                  value: String)


}
