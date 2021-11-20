package Api

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.config.{Config, ConfigFactory}
import objectMapper._
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair

import java.io.{BufferedWriter, File, FileWriter}
import java.text.SimpleDateFormat
import java.time.{Instant, LocalDateTime, ZoneId}
import java.util
import java.util.TimeZone
import scala.io.Source
import scala.util.Try


class alpha {
  var reporter: Option[reportHome] = None
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
  var sys_sn = config.getString("alphaess.system_sn")

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
         //TODO figure out fresh token command
          Login();
          run()
        }
        else{
          getMetrics()
        }
    }
  }

  def Login():LoginReply ={
    println("Calling Login()")
    val urlExtension= "/api/Account/Login"
    val reply = restCaller.simpleRestPostCall(eplBaseHost+urlExtension,new LoginDetails(username,password))
    val result: LoginReply = jsonMapper.readValue(reply, classOf[LoginReply])

    result.data match {
      case null => println("ERROR : " + result.toString)
      case value : Any =>  writeToken(Some(value))
    }
    result
  }

  def refreshToken(): token = {
   // println("Calling refreshToken()")
   // val urlExtension= "Api/Account/Login"
   // val reply = restCaller.get(eplBaseHost+urlExtension, readToken)
   // val result = jsonMapper.readValue(reply, classOf[token])
   // writeToken(Some(result))
  //  result
    token.empty()
  }

  def getMetrics() = {
    println("Calling getMetrics ")
    val urlExtension= "/api/ESS/GetSecondDataBySn?sys_sn="+sys_sn+"&noLoading=true"
    val token = readToken
    val postParameters = new util.ArrayList[NameValuePair](2);
    postParameters.add(new BasicNameValuePair("sys_sn", sys_sn));
    postParameters.add(new BasicNameValuePair("noLoading", "true"));
    val reply = restCaller.simpleRestGetCall(eplBaseHost+urlExtension,
      withToken = true,
      token = token.AccessToken,
      withParameters = true,
      parameters = postParameters)
    val metrics = (jsonMapper.readValue(reply, classOf[SystemDetailsReply]).data)
    reporter.getOrElse(new reportHome(metrics.sn)).write(metrics)
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
