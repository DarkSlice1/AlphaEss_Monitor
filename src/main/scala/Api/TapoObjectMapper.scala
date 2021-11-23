package Api

import com.google.gson.Gson
import me.chanjar.weixin.common.util.crypto.PKCS7Encoder

import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.{KeyPairGenerator, SecureRandom}
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
                                 username:String,
                                 password:String
                               )

  case class SecurePassthroughPayload(
                                       method:String,
                                       params :SecurePassthroughPayloadParams
                                     )
  case class SecurePassthroughPayloadParams(request:String)

  class C658a @throws[Exception]
  (val bArr: Array[Byte], val bArr2: Array[Byte])
  {
    val key = new SecretKeySpec(bArr, "AES")
    val iv = new IvParameterSpec(bArr2)

    val f21776a_enc = Cipher.getInstance("AES/CBC/PKCS5Padding")
    f21776a_enc.init(Cipher.ENCRYPT_MODE, key, iv)
    val f21777b_dec = Cipher.getInstance("AES/CBC/PKCS5Padding")
    f21777b_dec.init(Cipher.DECRYPT_MODE, key, iv)


//    val instance = KeyPairGenerator.getInstance("RSA")
//    instance.initialize(1024, new SecureRandom())
//    val generateKeyPair = instance.generateKeyPair()
//    val a: RSAPublicKey = generateKeyPair.getPublic().asInstanceOf[RSAPublicKey]
//    val b: RSAPrivateKey = generateKeyPair.getPrivate().asInstanceOf[RSAPrivateKey]
//    val str = new String(Base64.getEncoder.encode(a.getEncoded()))
//    val str2 = new String(Base64.getEncoder.encode(b.getEncoded()))


    def decrypt(cipherText: String): String = {
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      cipher.init(Cipher.DECRYPT_MODE, key, iv)
      val plainText = cipher.doFinal(Base64.getDecoder.decode(cipherText))
      new String(plainText)
    }

    def encrypt( input: String): String = {
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      cipher.init(Cipher.ENCRYPT_MODE, key, iv)
      val cipherText = cipher.doFinal(input.getBytes)
      Base64.getEncoder.encodeToString(cipherText)
    }

//    println("Text ="+testString)
//
//    val enc = encrypt(testString)
//    val plainText: String = decrypt(enc)
//
//    println("Encrypted ="+enc)
//    println("Decrypted ="+plainText)

//    val encr = Base64.getEncoder().encodeToString(enc)
//    val encry = encr.replace("\r\n", "")
//
//    println("encrypted ="+encry)
//    val dec = f21777b_dec.doFinal(Base64.getDecoder.decode(encry))
//    val decr = new String(dec)
//    println("decrypted ="+decr)

//    @throws[Exception]
//    def mo38009b_enc(str: String): String = {
//      val doFinal = f21776a_enc.doFinal(str.getBytes)
//      val encrypted = Base64.getEncoder().encodeToString(doFinal)
//      encrypted.replace("\r\n", "")
//    }
//
//    @throws[Exception]
//    def mo38006a_dec(str: String): String = {
//
//      val plainText = f21777b_dec.doFinal(Base64.getDecoder.decode(str))
//      new String(plainText)

      //val doFinal = f21777b_dec.doFinal(new KspB64().decode(str.getBytes("utf-8")))
      //doFinal.toString
//    }
  }

  class KspKeyPair(xprivateKey: String, xpublicKey: String) {
    val privateKey = xprivateKey
    val publicKey = xpublicKey

    def getPrivateKey() : Array[Byte] ={
       privateKey.getBytes()
    }

    def getPublicKey() : Array[Byte] ={
       publicKey.getBytes()
    }
  }
  class KspB64 {
    def decode (bytes : Array[Byte] ): Array[Byte] =
      {
       Base64.getMimeDecoder().decode(bytes)
      }
  }
}