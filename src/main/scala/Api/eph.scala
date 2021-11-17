package Api

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.config.{Config, ConfigFactory}
import objectMapper._
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}

import java.io.{BufferedWriter, File, FileWriter}
import java.text.SimpleDateFormat
import java.time.{Instant, LocalDateTime, ZoneId}
import java.util.TimeZone
import scala.io.Source
import scala.util.Try


class alpha {
  val reporter = new reportHome()
  val jsonMapper = new ObjectMapper()
  val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  df.setTimeZone(TimeZone.getTimeZone("UTC"));
  jsonMapper.registerModule(DefaultScalaModule)
  jsonMapper.registerModule(new JavaTimeModule())
  jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  jsonMapper.setDateFormat(df);

  val config: Config = ConfigFactory.load()
  var username = config.getString("alphaess.username")
  var password = config.getString("alphaess.password")

  val eplBaseHost = "https://www.alphaess.com"
  val tokenFile = new File(getClass.getResource("/lastToken.txt").getFile())

  def run(): Unit = {
    //Do we have an access token
    readToken() match {
      // No - empty token object returned
      case token if (token.AccessToken == "") => Login(); run;
      // Yes
      case token =>
        val expiry = LocalDateTime.ofInstant(token.TokenCreateTime.toInstant,ZoneId.of("UTC")).plusSeconds(token.ExpiresIn.toLong)
        val today = LocalDateTime.ofInstant(Instant.now(),ZoneId.of("UTC"))
        if(today.isAfter(expiry)){
          //token expired - lets refresh
          println("Token is Expired")
          //refreshToken()
          run()
        }
        else{
          //getHomeById()
        }
    }
  }

  def Login():Login ={
    println("Calling Login()")
    val urlExtension= "/api/Account/Login"
    val reply = restCaller.simpleRestPostCall(eplBaseHost+urlExtension,username,password)
    val result: Login = jsonMapper.readValue(reply, classOf[Login])

    result.data match {
      case null => println("ERROR : " + result.toString)
      case value : Any =>  writeToken(Some(value))
    }
    result
  }

  def refreshToken(): token = {
    println("Calling refreshToken()")
    val urlExtension= "Account/RefreshToken"
    val reply = restCaller.get(eplBaseHost+urlExtension, readToken)
    val result = jsonMapper.readValue(reply, classOf[token])
    writeToken(Some(result))
    result
  }

  def getHomeById() = {
    println("Calling getHomeById ")
    //val urlExtension= "Home/GetHomeById"
    //val token = readToken
    //val reply = restCaller.get(eplBaseHost+urlExtension, token, Map("homeId"->token.currentHomeId.toString))
    //reporter.write(jsonMapper.readValue(reply, classOf[getHomeById]))
  }


  def readToken() :token = {
    val bufferedSource = Source.fromFile(tokenFile)
    val token = Try (jsonMapper.readValue(bufferedSource.mkString, classOf[token]))
    bufferedSource.close
    //return token or an empty expired token
    token.getOrElse(objectMapper.token.empty())
  }

  def writeToken(token : Option[token])= {
    val bw = new BufferedWriter(new FileWriter(tokenFile))
    token match{
      case Some(value) => println("Access Token received : " + value.AccessToken + ", expires on "+value.ExpiresIn+" minutes, created at "+value.TokenCreateTime)
                  bw.write(jsonMapper.writeValueAsString(value))
      //clean the file
      case None =>  bw.write("")
    }
    bw.close()
  }

}
