package api.myenergi

import api.common.FileIO._
import api.myenergi.MyEnergiObjectMapper._
import com.typesafe.config.Config
import metrics.KamonMetrics

//availaible endpoints : https://github.com/claytonn73/myenergi_api/blob/62d6915784bde9aaaa6fbfe34cc0ec6eeb2eb060/myenergi/const.py#L9

class myenergi_zappie(config: Config, reporterKamon : KamonMetrics) {


  val username = config.getString("myenergi.username")
  val password = config.getString("myenergi.password")
  val myenergi_BaseHost = "https://director.myenergi.net"
  var asn_url = ""

  def Run(): Unit = {

    try {
      asn_url match {
        // No - empty url
        case "" => Login();getMetrics();
        // Yes
        case _ => getMetrics()
      }
    }
    catch {
      case ex: Exception => println("Zappie ERROR: " + ex.toString + " Trying to login again");
        //reset
        asn_url = ""
    }
  }


  def Login() = {
    val urlExtension = ""
    val reply = restCaller.simpleRestGetCallDigest(
      url = myenergi_BaseHost + urlExtension,
      username = username,
      password = password,
      host = myenergi_BaseHost.replace("https://",""),
      digestUri = urlExtension
    )
    asn_url  = jsonMapper.readValue(reply, classOf[DigestReplyDetails]).asn
  }

  def getMetrics() = {
    val urlExtension = "/cgi-jstatus-Z"
    val reply = restCaller.simpleRestGetCallDigest(
      url = "https://"+asn_url + urlExtension,
      username = username,
      password = password,
      host = asn_url,
      digestUri = urlExtension
    )
   jsonMapper.readValue(reply, classOf[jstatusZReply])

    //TODO MAP metrics
  }
}