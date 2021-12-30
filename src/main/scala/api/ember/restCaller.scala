package api.ember

import api.common.RestBody
import com.google.gson.Gson
import org.apache.http.NameValuePair
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import java.util.ArrayList

object restCaller {

  val timeout = 1000

  def simpleRestPostCall(url: String, data: RestBody,
                         hostname :String,
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

  def simpleRestGetCall(url: String,
                        withParameters: Boolean = false,
                        parameters: ArrayList[NameValuePair] = new ArrayList(),
                        withToken: Boolean = false,
                        token: String = "",
                        hostname :String): String = {

    // create an HttpGet object
    val uri = url
    val get = new HttpGet(uri)
    if (withParameters) {
      new URIBuilder(get.getURI()).addParameters(parameters).build()
    }

    // set the Content-type
    get.setHeader("Content-type", "application/json")
    get.setHeader("Host", hostname)
    if (withToken) {
      get.setHeader("Authorization", token)
    }
    // send the get request
    val response = (new DefaultHttpClient).execute(get)
    // print the response headers
    EntityUtils.toString(response.getEntity, "UTF-8")
  }
}
