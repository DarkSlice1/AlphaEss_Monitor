package api.common

import com.fasterxml.jackson.annotation.{JsonFormat, JsonIgnoreProperties, JsonInclude}
import java.time.temporal.ChronoUnit
import java.time.Instant
import java.util.Date

object Token {

  def empty(): Token = {
    new Token("", 0, Date.from(Instant.now()).toString, "")
  }
}

@JsonIgnoreProperties(ignoreUnknown=true)
case class Token(
                  AccessToken: String,
                  ExpiresIn: Double = 36000.0,
                  //@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy/MM/dd 下午HH:mm:ss", timezone="GMT") // format keeps changing ... 3rd time changing in 3 months
                  TokenCreateTime: String = "",
                  RefreshTokenKey: String)
