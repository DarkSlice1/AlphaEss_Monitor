package api.common


import com.fasterxml.jackson.annotation.JsonFormat

import java.time.{Instant, LocalDateTime}
import java.util.Date

object Token {

  def empty(): Token = {
    new Token("", 0, Date.from(Instant.now()), "")
  }
}

case class Token(
                  AccessToken: String,
                  ExpiresIn: Double = 36000.0,
                  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy/dd/MM HH:mm:ss", timezone="GMT")
                  TokenCreateTime: Date = new Date(),
                  RefreshTokenKey: String)
