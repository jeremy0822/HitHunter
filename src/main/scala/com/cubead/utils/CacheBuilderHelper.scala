package com.cubead.utils

import com.cubead.conf.Constants
import com.cubead.model.Conf
import com.ning.http.client.AsyncHttpClient

/**
 * Created by xiaoao on 5/21/15.
 * cache configuration data for hit hunter
 */
object CacheBuilderHelper {
  import org.json4s._
  import org.json4s.native.JsonMethods._
  implicit val formats = DefaultFormats

  val asyncHttpClient = new AsyncHttpClient()

  def buildCache(): Unit ={
    cacheIt(Constants.CACHE_SOURCE_URL)
  }

  def buildCacheById(tenantId: String): Unit ={
    val url = s"${Constants.CACHE_SOURCE_URL}?tid=${tenantId}"
    cacheIt(url)
  }

  def cacheIt(url: String) {
    val f = asyncHttpClient.prepareGet(url).execute()
    val code = f.get().getStatusCode
    if(code.equals(200)) {
      val body = f.get.getResponseBody;
      val o = parse(body).extract[List[Conf]]
      CacheManager.setConf(o)
    }else{
      println("error: get an error ", code)
    }
  }

}
