package api.common

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.scalalogging.LazyLogging

import java.io.{BufferedWriter, File, FileWriter}
import java.text.SimpleDateFormat
import java.util.TimeZone
import scala.io.Source
import scala.util.Try

object FileIO extends LazyLogging{

  val jsonMapper = new ObjectMapper()
  val df = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
  df.setTimeZone(TimeZone.getTimeZone("GMT"));
  jsonMapper.registerModule(DefaultScalaModule)
  jsonMapper.registerModule(new JavaTimeModule())
  jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  jsonMapper.setDateFormat(df);

  def readToken(tokenFile:File ) :Token = {
    val bufferedSource = Source.fromFile(tokenFile)
    val token = Try (jsonMapper.readValue(bufferedSource.mkString, classOf[Token]))
    bufferedSource.close
    //return token or an empty expired token
    token.getOrElse(Token.empty())
  }

  def writeToken(tokenFile:File, token : Option[Token])= {
    val bw = new BufferedWriter(new FileWriter(tokenFile))
    token match{
      case Some(value) => logger.info("Access Token received : " + value.token + ", expires on "+value.ExpiresIn+" minutes, created at "+value.TokenCreateTime)
        bw.write(jsonMapper.writeValueAsString(value))
      //clean the file
      case None =>  bw.write("")
    }
    bw.close()
  }
}
