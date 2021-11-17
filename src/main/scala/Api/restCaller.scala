package Api

import Api.objectMapper.{LoginDetails, token}
import com.google.gson.Gson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils

import java.io.DataOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object restCaller {

  val timeout = 1000

  def get(url: String,
          tokenObj: token,
          parameters: Map[String, String] = Map.empty,
          requestMethod: String = "GET",
          connectTimeout: Int = timeout,
          readTimeout: Int = timeout): String = {
    import java.net.{HttpURLConnection, URL}
    val realUrl = if (parameters != Map.empty)
      url + "?" + getDataString(parameters)
    else
      url
    val connection = (new URL(realUrl)).openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestProperty("Authorization", "bearer " + tokenObj.AccessToken);
    connection.setConnectTimeout(connectTimeout)
    connection.setReadTimeout(readTimeout)
    connection.setRequestMethod(requestMethod)
    val inputStream = connection.getInputStream
    val content = scala.io.Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close
    content
  }


  def urlEncoded(url: String,
                 requestMethod: String = "POST",
                 connectTimeout: Int = timeout,
                 readTimeout: Int = timeout,
                 parameters: String = "") : String = {
    import java.net.{HttpURLConnection, URL}
    val postData = parameters.getBytes(StandardCharsets.UTF_8)
    val postDataLength = postData.length;
    val connection = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    connection.setDoOutput(true)
    connection.setConnectTimeout(connectTimeout)
    connection.setReadTimeout(readTimeout)
    connection.setRequestMethod(requestMethod)
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setRequestProperty("Host", "www.alphaess.com")
    connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
    new DataOutputStream(connection.getOutputStream()) {
      write(postData)
      flush()
    }
    val inputStream = connection.getInputStream
    val content = scala.io.Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close
    content
  }


  def getDataString(params: Map[String, String]): String = {
    val result = new StringBuilder()
    var first = true
    params.foreach(entry => {
      if (first)
        first = false;
      else
        result.append("&");
      result.append(URLEncoder.encode(entry._1, "UTF-8"));
      result.append("=");
      result.append(URLEncoder.encode(entry._2, "UTF-8"));
    })
    result.toString();
  }


  def simpleRestPostCall(url: String,username:String, password:String) :String = {
    // create a Stock object
    val stock = new LoginDetails(username, password)
    // convert it to a JSON string
    val stockAsJson = new Gson().toJson(stock)
    // create an HttpPost object
    val post = new HttpPost(url)
    // set the Content-type
    post.setHeader("Content-type", "application/json")
    post.setHeader("Host", "www.alphaess.com")
    // add the JSON as a StringEntity
    post.setEntity(new StringEntity(stockAsJson))
    // send the post request
    val response = (new DefaultHttpClient).execute(post)
    // print the response headers
    EntityUtils.toString(response.getEntity, "UTF-8")
  }
}
