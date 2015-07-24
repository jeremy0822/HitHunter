package com.cubead.utils

import com.cubead.model.Conf
import scala.collection.immutable.Map
//import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by xiaoao on 5/20/15.
 */
object CacheManager {
//  import scalacache._
//  import lrumap._
//  import memoization._

  // Just specified a maximum cache size in elements
//  implicit val scalaCache = ScalaCache(LruMapCache(100000))

  var CacheMap = Map[String, String]()

  //  import scalacache._
  //
  //  import ehcache._
  //
  //  val cacheManager: net.sf.ehcache.CacheManager = net.sf.ehcache.CacheManager.newInstance()
  //  val underlying: net.sf.ehcache.Cache = cacheManager.getCache("myCache")
  //  implicit val scalaCache = ScalaCache(EhcacheCache(underlying))

  //  val underlyingGuavaCache = CacheBuilder.newBuilder().maximumSize(10000L).build[String, Object]
  //  implicit val scalaCache = ScalaCache(EhcacheCache(underlyingGuavaCache))

  def getConf(tenantId: String): Option[Conf] = {
    val c = CacheMap.get(tenantId)
    c match {
      case Some(content) => {
    	  val m = content.split(",").toList.map(_.toInt)
    		Some(Conf(tenantId, m))
      }
      case None => {
    	  None
      }
    }
  }

//  def getConf(tenantId: String): Conf = memoize {
////    val l = get(tenantId).value.get.get.asInstanceOf[List[Int]]
////    new Conf(tenantId, l)
//    var conf: Conf = null
//    get[String](tenantId).onComplete{ r =>
//      val v = r.get
//      val c = v.get.split(",").toList.map(_.toInt)
//      println(s"c: ${c}")
//      conf = Conf(tenantId, c)
////      println(s"value: ${v.get}")
//    }
//    conf
//
//    val m = CacheMap.get(tenantId).get.split(",").toList.map(_.toInt)
//    Conf(tenantId, m)
//
////    new Conf("119378", List(2,2,10,5,100,10))
//    //value.get.get.get.asInstanceOf[Conf]
//  }

  def setConf(conf: List[Conf]): Unit ={
    conf.foreach{ c =>
      CacheMap += (c.tid -> c.rule.mkString(","))
      println(s"${c.tid}:${CacheMap.get(c.tid)}")
    }
  }
}
