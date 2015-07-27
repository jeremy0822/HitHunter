package com.cubead.actors

import akka.actor.{ActorLogging, Props, Actor}
import akka.actor.Actor.Receive
import com.cubead.conf.Constants
import com.cubead.model.{WorkResult, LogItem}
import com.cubead.utils.CacheManager
import redis.RedisClient
import redis.api.Limit

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by xiaoao on 5/18/15.
 */

object ResultActor{
  def props(redis: RedisClient): Props = Props(new ResultActor(redis))
}

class ResultActor(redis: RedisClient) extends Actor with ActorLogging{

  override def receive: Receive = {
    case message: WorkResult =>
      val tb = message.toTBName

      var records = redis.zrangebyscore(tb, new Limit(message.timestamp - message.duration, true), new Limit(message.timestamp, true))

      records.onComplete { record =>
        record.foreach{ r =>
          val times = for{
            b <- r
          }yield {
              val c = new String(b.toArray, "UTF-8")
              c.substring(0, c.indexOf("_"))
            }
          val s = times.mkString(",")
//          println(s"－－－－－${message.duration}, ${s}")
          val value = s"${message.tenantId},${message.ip},${message.onuid},${message.timestamp},${message.duration},[${s}]"
          redis.rpush(Constants.HR_KEY, value)
        }
      }
  }
}
