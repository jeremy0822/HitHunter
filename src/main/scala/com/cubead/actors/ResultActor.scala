package com.cubead.actors

import akka.actor.{ ActorLogging, Props, Actor }
import akka.actor.Actor.Receive
import com.cubead.conf.Constants
import com.cubead.model.{ WorkResult, LogItem }
import com.cubead.utils.CacheManager
import redis.RedisClient
import redis.api.Limit
import scala.concurrent.ExecutionContext.Implicits.global
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Created by xiaoao on 5/18/15.
 */

object ResultActor {
  def props(redis: RedisClient): Props = Props(new ResultActor(redis))
}

class ResultActor(redis: RedisClient) extends Actor with ActorLogging {
  
  val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  override def receive: Receive = {
    case message: WorkResult =>
      val tb = message.toTBName
      var startTime: Long = System.currentTimeMillis()

      var records = redis.zrangebyscore(tb, new Limit(message.timestamp - message.duration.*(1000), true), new Limit(message.timestamp, true))

      records.onComplete { record =>
        record.foreach { r =>
          val times = for {
            b <- r
          } yield {
            val c = new String(b.toArray, "UTF-8")
            c.substring(0, c.indexOf("_"))
          }
          val s = times.mkString(",")
          val value = s"${message.tenantId},${message.ip},${message.onuid},${message.timestamp.toLong},${message.duration},[${s}]"
          redis.rpush(Constants.HR_KEY, value)
        }
      }
      println(s"[${format.format(new Date)}](${ResultActor.getClass})save result to redis:(tenantId:${message.tenantId},ip:${message.ip},OWN_UID:${message.onuid}),cost ${(System.currentTimeMillis() - startTime)} ms.")
  }
}
