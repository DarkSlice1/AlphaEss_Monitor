package api.alpha

import api.alpha.AlphaObjectMapper._
import api.common.FileIO._
import api.common.Token
import com.typesafe.config.Config
import metrics.KamonMetrics
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair

import java.io.File
import java.time.{Instant, LocalDateTime, ZoneId}
import java.util

class alpha(config: Config, reporterKamon : KamonMetrics) {


  var username = config.getString("alphaess.username")
  var password = config.getString("alphaess.password")
  var sys_sn = config.getString("alphaess.system_sn")

  var reporter = new reportHome(config,reporterKamon)

  val eplBaseHost = "https://www.alphaess.com"
  var token = Token.empty()

  def run(): Unit = {
    //Do we have an access token
    token match {
      // No - empty token object returned
      case token if (token.AccessToken == "") => Login(); run;
      // Yes
      case token =>
        val expiry = LocalDateTime.ofInstant(token.TokenCreateTime.toInstant,ZoneId.of("UTC")).plusSeconds(token.ExpiresIn.toLong)
        val today = LocalDateTime.ofInstant(Instant.now(),ZoneId.of("UTC"))
        if(today.isAfter(expiry)){
          //token expired - lets refresh
          println("Alpha Token is Expired")
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
    val urlExtension= "/api/Account/Login"
    val reply = restCaller.simpleRestPostCall(eplBaseHost+urlExtension,new LoginDetails(username,password))
    val result: LoginReply = jsonMapper.readValue(reply, classOf[LoginReply])

    result.data match {
      case null => println("ERROR : " + result.toString)
      case value : Any =>  token = value
    }
    result
  }

  def refreshToken(): Token = {
   // println("Calling refreshToken()")
   // val urlExtension= "Api/Account/Login"
   // val reply = restCaller.get(eplBaseHost+urlExtension, readToken)
   // val result = jsonMapper.readValue(reply, classOf[token])
   // writeToken(Some(result))
  //  result
    Token.empty()
  }

  def getMetrics() = {
    //println("Calling getMetrics ")
    val urlExtension= "/api/ESS/GetSecondDataBySn?sys_sn="+sys_sn+"&noLoading=true"
    val postParameters = new util.ArrayList[NameValuePair](2);
    postParameters.add(new BasicNameValuePair("sys_sn", sys_sn));
    postParameters.add(new BasicNameValuePair("noLoading", "true"));
    val reply = restCaller.simpleRestGetCall(eplBaseHost+urlExtension,
      withToken = true,
      token = token.AccessToken,
      withParameters = true,
      parameters = postParameters)
    val metrics = (jsonMapper.readValue(reply, classOf[SystemDetailsReply]).data)
    println("AlphaEss Metrics Completed")
    reporter.write(metrics)
  }


  def resetDailyCounter(): Unit =
  {
    reporter.DailySolarGeneration = 0
  }
}
