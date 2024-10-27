package api.alpha

import org.apache.http.NameValuePair
import org.apache.http.client.methods.{HttpGet, HttpPost, HttpPut}
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.{ByteArrayEntity, StringEntity}
import org.apache.http.impl.client.{DefaultHttpClient, HttpClientBuilder}
import org.apache.http.util.EntityUtils

import java.util.ArrayList

object restCaller {


  def simpleRestPostCall(url: String, data: String,
                         withToken: Boolean = false,
                         token: String = ""): String = {
    // create an HttpPost object
    val post = new HttpPost(url)

    // set the Content-type
    post.setHeader("Content-type", "application/json")
    //post.setHeader("Host", "www.alphaess.com")
    if (withToken) {
      post.setHeader("Authorization", "Bearer " + token)
    }
    val entity = new ByteArrayEntity(data.getBytes("UTF-8"));

    post.setEntity(entity);
    // send the post request
    val  response = (HttpClientBuilder.create().build()).execute(post)
    // print the response headers
    EntityUtils.toString(response.getEntity, "UTF-8")
  }

  def simpleRestPutCall(url: String, data: String,
                         withToken: Boolean = false,
                         token: String = ""): String = {
    val post = new HttpPut(url)

    post.setHeader("Content-type", "application/json")
    //post.setHeader("Host", "www.alphaess.com")
    if (withToken) {
      post.setHeader("Authorization", "Bearer " + token)
    }
    val entity = new ByteArrayEntity(data.getBytes("UTF-8"));
    post.setEntity(entity);
    // send the post request
    val response = (HttpClientBuilder.create().build()).execute(post)
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
    if (withToken) {
      get.setHeader("Authorization", "Bearer " + token)
    }
    // send the get request
    val response = (HttpClientBuilder.create().build()).execute(get)
    // print the response headers
    EntityUtils.toString(response.getEntity, "UTF-8")
  }
}
