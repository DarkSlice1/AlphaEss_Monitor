package Api

import Api.TapoObjectMapper._
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.codec.binary.Hex
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import java.util.Base64
import java.security.spec.PKCS8EncodedKeySpec
import java.security.{KeyFactory, MessageDigest}
import javax.crypto.Cipher

class Tapo {

  val config: Config = ConfigFactory.load()
  var username = config.getString("tapo.username")
  var password = config.getString("tapo.password")
  import collection.JavaConversions._
  var addresses = config.getStringList("tapo.addresses").toList(0)

  val jsonMapper = new ObjectMapper()
  jsonMapper.registerModule(DefaultScalaModule)
  jsonMapper.registerModule(new JavaTimeModule())
  jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  val privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMY+gDb9hwm9gmYB+iDYxKPngQ3XRm3U4tzea9NLtmHgWipM44tE4zWkEeaBawWbFjrnT0lPriTXPn6OvvDRCY7eRejB9wP/aezoNMMrA2XQ6Du8gcopM1ylUJCWjr9nftTb6wn75wY5IMPUKM2N0uZ+dvfQH4qEQwE05z6Iz1szAgMBAAECgYAW5UcHktZSwKlbwKSzwHVNfMJB5/gBXVHqMmH/oEHrIe8n7YNmJUmce1t55L6IgjXaDbbxf5tcM+PK2A+jXnEc8+2snDbI1PpBepEwg5vzZ8RYnhGZT6P0/PsIqInkTIZnfXDnQ6wBapO1m9xJDe/BfyWMVRa6JqJtgQ0XpfG76QJBAOgKPSF5wJKC69lCbvyh38fQoeJxEKrD03FGXGBDLLYOHqfijdMTvcnE415994MI3fTy2dWm/yB8wZ7DvYVwVX8CQQDatuZyi6LhLhU47l5vpFsOnRWGhGrZaIN0o7N/1v5Vwieujrwy/yW2uCvUeXVnmohJa+sFSr29HO4PEQwWtFxNAkBXSxrKUDJ5K9Wsa0izs/YrBrsQJDb/9yHBmJXCBSN57f/sateuE9wvXummr78AxcIyl3YJ4YRTZXu1za+r1qHjAkEAzjkavPKQ18W92PpZLOdJvFO9EiMVJH15Rad8/pNXKMFy7RJEvcj6ZHjvSt5jJxb8Xk5VQZ4hnYkDpk0qmtXhGQJBAIO6TEXkHU3Sn0qTZsPmpu+EfCADLNKG43kiv74+cRzS7Th2A6E1yq5Y/lmbdpYaHqm0mKgvMHb2ls7DtRTYoXo="
  val publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDGPoA2/YcJvYJmAfog2MSj54EN10Zt1OLc3mvTS7Zh4FoqTOOLROM1pBHmgWsFmxY6509JT64k1z5+jr7w0QmO3kXowfcD/2ns6DTDKwNl0Og7vIHKKTNcpVCQlo6/Z37U2+sJ++cGOSDD1CjNjdLmfnb30B+KhEMBNOc+iM9bMwIDAQAB"
  val secretKey = "YyqsaPrgmVKcjS6UQY5aV0KtOdovqbuwGQMkZMoHtwygZQVy3fiLp0/03B4LKkYtIm0SBPYJw/cVw2DyZYsPPvX71RbuU9Pp+AJkWTfReEWhSW2vonWv6HIULK94hREHt7S51oBNXo5QP4wn2F4PD3hgP2DYBAw7guMy9wbGB+4="
  var Cookie = ""

  def run(): Unit = {
    val handShakeReply: HandShakeReply = handshake()
    val c658a = decode_handshake_key(handShakeReply.result.key)

    println("key = "+Hex.encodeHexString(c658a.bArr))
    println("iv = "+Hex.encodeHexString(c658a.bArr2))

    send_login(c658a)
  }


  def handshake() : HandShakeReply = {
    println("Calling Tapo.handshake()")
    val url = "http://" + addresses + "/app"

    val payload = HandShakePayload("handshake",
      HandShakePayloadParams("-----BEGIN PUBLIC KEY-----\n" +
        publicKey +
        "-----END PUBLIC KEY-----\n"), 0)

    val json = new Gson().toJson(payload)
    // create an HttpPost object
    val post = new HttpPost(url)
    // set the Content-type
    post.setHeader("Content-type", "application/json")
    // add the JSON as a StringEntity
    post.setEntity(new StringEntity(json))
    // send the post request
    val response = (new DefaultHttpClient).execute(post)
    // print the response headers
    val cookie = response.getHeaders("Set-Cookie")
    Cookie = cookie.head.getName+"="+cookie.head.getValue
    jsonMapper.readValue(EntityUtils.toString(response.getEntity, "UTF-8"), classOf[HandShakeReply])
  }

  def decode_handshake_key(key:String): C658a = {
      val decode: Array[Byte] = new KspB64().decode(key.getBytes("UTF-8"));
      val decode2: Array[Byte]= new KspB64().decode(privateKey.getBytes())
      val instance = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      val kf = KeyFactory.getInstance("RSA");
      val p = kf.generatePrivate(new PKCS8EncodedKeySpec(decode2));
      instance.init(Cipher.DECRYPT_MODE, p);
      val doFinal: Array[Byte] = instance.doFinal(decode);
      val bArr: Array[Byte] = new Array[Byte](16)
      val bArr2: Array[Byte] = new Array[Byte](16)
      System.arraycopy(doFinal, 0, bArr, 0, 16);
      System.arraycopy(doFinal, 16, bArr2, 0, 16);
      new C658a(bArr, bArr2);
  }

  def send_login(c658a: C658a) {
    println("Calling Tapo.Send_Login()")
    val url = "http://" + addresses + "/app"
    val loginPayload = LoginPayload("login_device",
      LoginPayloadParams(shaDigestUsername(username),
        new String(Base64.getEncoder.encode(password.getBytes("UTF-8")))),
      0)

    val loginPayloadJson = new Gson().toJson(loginPayload)
    val post = new HttpPost(url)
    post.setHeader("Cookie",Cookie)
    val EncryptedLoginPayloadJson = c658a.encrypt(loginPayloadJson)
    val secureRequest = SecurePassthroughPayload("securePassthrough",
      SecurePassthroughPayloadParams(EncryptedLoginPayloadJson))

    val securePassthroughJson = new Gson().toJson(secureRequest)
    post.setEntity(new StringEntity(securePassthroughJson))
    val reply = (new DefaultHttpClient).execute(post)
    println("Reply = "+ reply)
  }

  def encodedPassword(): Unit = {

  }


  def shaDigestUsername(str: String): String = {
    val bArr = str.getBytes
    val digest = MessageDigest.getInstance("SHA1").digest(bArr)
    val sb = new StringBuilder
    for (b <- digest) {
      val hexString = Integer.toHexString(b & 255)
      if (hexString.length == 1) {
        sb.append("0")
        sb.append(hexString)
      }
      else sb.append(hexString)
    }
    sb.toString
  }
}
