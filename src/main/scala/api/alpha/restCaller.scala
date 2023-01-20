package api.alpha


import api.common.RestBody
import com.google.common.base.Splitter
import com.google.common.hash.Hashing
import com.google.gson.Gson
import com.sun.org.apache.xerces.internal.impl.dv.xs.DateTimeDV
import org.apache.commons.codec.binary.Hex.encodeHexString
import org.apache.http.NameValuePair
import org.apache.http.client.methods.RequestBuilder.post
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils

import java.nio.charset.StandardCharsets
import java.time.{Instant, LocalDateTime}
import java.util.ArrayList

object restCaller {

  val timeout = 1000
  val authconstant = "LS885ZYDA95JVFQKUIUUUV7PQNODZRDZIS4ERREDS0EED8BCWSS"

  def simpleRestPostCall(url: String, data: RestBody,
                         withToken: Boolean = false,
                         token: String = ""): String = {
    // convert it to a JSON string
    val json = new Gson().toJson(data)
    // create an HttpPost object
    val post = new HttpPost(url)

    val authtimestamp = Splitter.fixedLength(10).split(Instant.now().toEpochMilli().toString).iterator().next() //"1666030462"
    post.setHeader("authtimestamp", authtimestamp)
    val constant_with_timestamp = authconstant + authtimestamp
    val authsignature = "al8e4s" + Hashing.sha512().hashString(constant_with_timestamp,StandardCharsets.UTF_8) + "ui893ed"
    post.setHeader("authsignature", authsignature)
    // set the Content-type
    post.setHeader("Content-type", "application/json")
    post.setHeader("Host", "www.alphaess.com")
    if (withToken) {
      post.setHeader("Authorization", "Bearer " + token)
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
                        token: String = ""): String = {

    // create an HttpGet object
    val uri = url
    val get = new HttpGet(uri)
    if (withParameters) {
      get.setURI(new URIBuilder(get.getURI()).addParameters(parameters).build())
    }

    val authtimestamp = Splitter.fixedLength(10).split(Instant.now().toEpochMilli().toString).iterator().next()
    get.setHeader("authtimestamp", authtimestamp)
    val constant_with_timestamp = authconstant + authtimestamp
    val authsignature = "al8e4s" + Hashing.sha512().hashString(constant_with_timestamp,StandardCharsets.UTF_8) + "ui893ed"
    get.setHeader("authsignature", authsignature)

    // set the Content-type
    get.setHeader("Content-type", "application/json")
    get.setHeader("Host", "www.alphaess.com")
    if (withToken) {
      get.setHeader("Authorization", "Bearer " + token)
    }


    //post.setEntity(new StringEntity(json))
    // send the get request
    val response = (new DefaultHttpClient).execute(get)
    // print the response headers
    EntityUtils.toString(response.getEntity, "UTF-8")
  }
}
