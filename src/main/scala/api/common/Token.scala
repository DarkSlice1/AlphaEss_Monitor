package api.common


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
                  TokenCreateTime: Date = new Date(),
                  RefreshTokenKey: String)
