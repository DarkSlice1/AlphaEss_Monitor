package api.myenergi

import api.common.RestBody
import com.google.gson.Gson
import org.apache.http.NameValuePair
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpStatus
import org.apache.http.auth.AUTH
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.auth.DigestScheme
import org.apache.http.impl.client.BasicResponseHandler

import java.util.{ArrayList, Date, Random}

object restCaller {

  val timeout = 1000

  def simpleRestPostCall(url: String, data: RestBody,
                         hostname: String,
                         withToken: Boolean = false,
                         token: String = ""): String = {
    // convert it to a JSON string
    val json = new Gson().toJson(data)
    // create an HttpPost object
    val post = new HttpPost(url)
    // set the Content-type
    post.setHeader("Content-type", "application/json")
    post.setHeader("Host", hostname)

    if (withToken) {
      post.setHeader("Authorization", token)
    }

    // add the JSON as a StringEntity
    post.setEntity(new StringEntity(json))
    // send the post request
    val response = (new DefaultHttpClient).execute(post)
    // print the response headers
    EntityUtils.toString(response.getEntity, "UTF-8")
  }

  def simpleRestGetCallDigest(url: String,
                              username: String = "",
                              password: String = "",
                              host :String,
                              digestUri:String): String = {

    val httpclient = new DefaultHttpClient
    val httpclient2 = new DefaultHttpClient
    val httpget = new HttpGet(url)
    //System.out.println("Requesting : " + httpget.getURI)

    httpget.addHeader("HOST", host)
    val response = httpclient.execute(httpget)
    //System.out.println(response.getStatusLine)
    var reply = ""
    if (response.getStatusLine.getStatusCode == HttpStatus.SC_UNAUTHORIZED) { //Get current current "WWW-Authenticate" header from response
      val authHeader = response.getFirstHeader(AUTH.WWW_AUTH)
      //System.out.println("authHeader = " + authHeader)
      val digestScheme = new DigestScheme
      //Parse realm, nonce sent by server.
      digestScheme.processChallenge(authHeader)
      val creds = new UsernamePasswordCredentials(username, password)
      httpget.addHeader(digestScheme.authenticate(creds, new HttpGet(digestUri)))
      val responseHandler = new BasicResponseHandler
      reply = httpclient2.execute(httpget, responseHandler)
    }
    reply
  }
}