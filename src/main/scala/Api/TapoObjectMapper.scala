package Api

import java.util.Base64
import javax.crypto.{Cipher, SecretKey}
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object TapoObjectMapper {

  case class HandShakePayload(
                               method: String,
                               params: HandShakePayloadParams,
                               requestTimeMils: Int
                             )

  case class HandShakePayloadParams(
                                     key: String
                                   )

  case class HandShakeReply(
                             error_code: Int,
                             result: HandShakeReplyResult
                           )

  case class HandShakeReplyResult(key: String)

  case class LoginPayload(
                           method: String,
                           params: LoginPayloadParams,
                           requestTimeMils: Int
                         )

  case class LoginPayloadParams(
                                 username: String,
                                 password: String
                               )

  case class SecurePassthroughPayload(
                                       method: String,
                                       params: SecurePassthroughPayloadParams
                                     )

  case class SecurePassthroughPayloadParams(request: String)

}