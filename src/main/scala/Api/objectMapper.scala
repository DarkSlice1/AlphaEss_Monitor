package Api

import java.time.Instant
import java.util.Date

object objectMapper {

  """
    |{
    |  "token": { ... },
    |  "isSuccess": true,
    |  "message": null
    |}
    |"""

  case class Login(code:Int,info:String,data: token)

  """
    |{
    |  "emailAddress": "xxxx",
    |  "userId": 2000,
    |  "firstName": "xxxx",
    |  "lastName": "xxxx",
    |  "accessToken": "xxxxx",
    |  "profilePicture": null,
    |  "issuedOn": "2019-10-10T00:00:00Z",
    |  "expiresOn": "2020-01-08T00:00:00Z",
    |  "homeIds": [
    |    1000
    |  ],
    |  "currentHomeId": 1000,
    |  "fullName": "xxxx"
    |}
    |"""

  case class token(
                    AccessToken: String,
                    ExpiresIn: Double,
                    TokenCreateTime: Date,
                    RefreshTokenKey: String)

  case class LoginDetails(
                         username: String,
                         password: String
                         )

  case class getHomeById(
                          data: data,
                          isSuccess: Boolean,
                          message: String)

  """
    |{
    |  "homeId": 1000,
    |  "name": "xxxx",
    |  "gatewayId": "xxx",
    |  "isOnline": true,
    |  "gatewayDateTime": "2019-10-11T17:44:51",
    |  "lastRefreshed": "2019-10-11T16:44:52.5739068Z",
    |  "users": [ ... ],
    |  "receivers": [ ... ],
    |  "holiday": { ... },
    |  "inviteCode": "xxx",
    |  "weatherLocation": "xxxx",
    |  "weatherLocationId": "10000",
    |  "backlightModeIsActive": false,
    |  "frostProtectionEnabled": false,
    |  "frostProtectionTemperature": 0.0,
    |  "isFrostProtectionActive": false,
    |  "holidayModeActive": false,
    |  "activeZoneBoosts" [ ... ]
    |}
    |"""

  case class data(
                   homeId: Int,
                   name: String,
                   gatewayId: String,
                   isOnline: Boolean,
                   //@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
                   gatewayDateTime: Instant,
                   lastRefreshed: Instant,
                   users: List[users],
                   receivers: List[receivers],
                   holiday: holiday,
                   inviteCode: String,
                   weatherLocation: String,
                   weatherLocationId: String,
                   backlightModeIsActive: Boolean,
                   frostProtectionEnabled: Boolean,
                   frostProtectionTemperature: Double,
                   isFrostProtectionActive: Boolean,
                   holidayModeActive: Boolean,
                   activeZoneBoosts: List[activeZoneBoosts])

  """
    |{
    |  "homeId": 9355,
    |  "startDate": null,
    |  "endDate": null,
    |  "status": 0,
    |  "scheduledByUserId": 17481
    |}
    |"""

  case class holiday(
                      homeId: Int,
                      startDate: String,
                      endDate: String,
                      status: Int,
                      scheduledByUserId: Int)

  """
    |{
    |  "zoneBoostActivationId": 1000,
    |  "zoneId": 1000,
    |  "zoneName": "xxx",
    |  "userId": 1000,
    |  "activatedByUserName": "xxx",
    |  "numberOfHours": 0,
    |  "targetTemperature": 00.00,
    |  "activatedOn": "2019-10-09T11:58:40.497",
    |  "finishDateTime": "2019-10-09T12:58:40.497",
    |  "expiryTime": "2019-10-09T12:58:40.497",
    |  "wasCancelled": false,
    |  "comments": null
    |}
    |"""

  case class activeZoneBoosts(
                               zoneBoostActivationId: Int,
                               zoneId: Int,
                               zoneName: String,
                               userId: Int,
                               activatedByUserName: String,
                               numberOfHours: Int,
                               targetTemperature: Double,
                               activatedOn: String,
                               finishDateTime: String,
                               expiryTime: String,
                               wasCancelled: Boolean,
                               comments: String)


  """
    |{
    |  "homeId": 1000,
    |  "homeName": "xxx",
    |  "userId": 2000,
    |  "userFullName": "xxx",
    |  "roleId": 0,
    |  "roleName": "xxxx",
    |  "roleStaticName": "xxxx",
    |  "accessPermissions": { ... },
    |  "favouriteScenarios": null
    |}
    |"""

  case class users(
                    homeId: Int,
                    homeName: String,
                    userId: Int,
                    userFullName: String,
                    roleId: Int,
                    roleName: String,
                    roleStaticName: String,
                    accessPermissions: accessPermissions,
                    favouriteScenarios: String)

  """
    |{
    |  "homeUserAccessId": 3000,
    |  "homeUserId": 2000,
    |  "homeManagement": true,
    |  "areaManagement": true,
    |  "schedulesManagement": true,
    |  "scenarioManagement": true,
    |  "eventManagement": true,
    |  "holidays": true,
    |  "boost": true
    |}
    |"""

  case class accessPermissions(
                                homeUserAccessId: Int,
                                homeUserId: Int,
                                homeManagement: Boolean,
                                areaManagement: Boolean,
                                schedulesManagement: Boolean,
                                scenarioManagement: Boolean,
                                eventManagement: Boolean,
                                holidays: Boolean,
                                boost: Boolean)

  """
    |{
    |  "receiverId": 1000,
    |  "homeId": 1000,
    |  "hardwareId": "xxx",
    |  "isOnline": true,
    |  "zones": [...]
    | }
    |"""

  case class receivers(
                        receiverId: Int,
                        homeId: Int,
                        hardwareId: String,
                        isOnline: Boolean,
                        zones: List[zones])
  """
    |{
    |  "zoneId": 1000,
    |  "receiverId": 1000,
    |  "receiverHardwareId": "xxx",
    |  "hardwareId": "xxx",
    |  "name": "XXX",
    |  "currentTemperature": 0.0,
    |  "targetTemperature": 0.0,
    |  "mode": 0,
    |  "isHotWater": false,
    |  "isOnline": false,
    |  "isBoostActive": false,
    |  "isAdvanceActive": false,
    |  "isDemo": false,
    |  "isTargetTemperatureReached": false,
    |  "isCurrentlyActive": false,
    |  "nextEventDate": null,
    |  "boostBaseDate": null,
    |  "programme": { ... },
    |  "areaId": null,
    |  "areaName": null,
    |  "boostActivations": [ ... ]
    |}
    |"""

  case class zones(
                    zoneId: Int,
                    receiverId: Int,
                    receiverHardwareId: String,
                    hardwareId: String,
                    name: String,
                    currentTemperature: Double,
                    targetTemperature: Double,
                    mode: Int,
                    isHotWater: Boolean,
                    isOnline: Boolean,
                    isBoostActive: Boolean,
                    isAdvanceActive: Boolean,
                    isDemo: Boolean,
                    isTargetTemperatureReached: Boolean,
                    isCurrentlyActive: Boolean,
                    nextEventDate: String,
                    boostBaseDate: String,
                    programme: programme,
                    areaId: String,
                    areaName: String,
                    boostActivations: List[boostActivations])

  """
    |{
    |  "zoneId": 1000,
    |  "monday": { ... },
    |  "tuesday": { ... },
    |  "wednesday": { ... },
    |  "thursday": { ... },
    |  "friday": { ... },
    |  "saturday": { ... },
    |  "sunday": { ... }
    |}
    |"""

  case class programme(
                        zoneId: Int,
                        monday: programmeDay,
                        tuesday: programmeDay,
                        wednesday: programmeDay,
                        thursday: programmeDay,
                        friday: programmeDay,
                        saturday: programmeDay,
                        sunday: programmeDay
                      )

  """
    |{
    |  "dayPeriodId": 1000,
    |  "period1": { ... },
    |  "period2": { ... },
    |  "period3": { ... }
    |}
    |"""

  case class programmeDay(
                           dayPeriodId: Int,
                           period1: period,
                           period2: period,
                           period3: period
                         )
  """
    |{
    |  "periodId": 1000,
    |  "startTime": "08:00:00",
    |  "endTime": "08:30:00",
    |  "type": 0,
    |  "isEnabled": false
    |"""

  case class period(
                     periodId: Int,
                     startTime: String,
                     endTime: String,
                     `type`: Int, //why.....
                     isEnabled: Boolean)

  """
    |{
    |  "zoneBoostActivationId": 10000,
    |  "zoneId": 10000,
    |  "zoneName": "xxx",
    |  "userId": 10000,
    |  "activatedByUserName": "xx",
    |  "numberOfHours": 0,
    |  "targetTemperature": 0.00,
    |  "activatedOn": "2019-10-07T22:42:34.83",
    |  "finishDateTime": "2019-10-07T23:42:34.83",
    |  "expiryTime": "2019-10-07T23:42:34.83",
    |  "wasCancelled": false,
    |  "comments": null
    |}
    |"""

  case class boostActivations(
                               zoneBoostActivationId: Int,
                               zoneId: Int,
                               zoneName: String,
                               userId: Int,
                               activatedByUserName: String,
                               numberOfHours: Int,
                               targetTemperature: Double,
                               activatedOn: String,
                               finishDateTime: String,
                               expiryTime: String,
                               wasCancelled: Boolean,
                               comments: String)

  object token {
    def empty(): token = {
      new token("",0, Date.from(Instant.now()),"")
    }
  }
}
