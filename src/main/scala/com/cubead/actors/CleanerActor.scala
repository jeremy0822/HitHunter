package com.cubead.actors

import akka.actor.{ Props, ActorLogging, Actor }
import akka.actor.Actor.Receive
import com.cubead.conf.Constants
import redis.RedisClient
import redis.api.Limit
import java.text.SimpleDateFormat
import java.util.Date

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by xiaoao on 5/18/15.
 */
object CleanerActor {
  def props(redis: RedisClient): Props = Props(new ResultActor(redis))
}

class CleanerActor(redis: RedisClient) extends Actor with ActorLogging {
  val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  override def receive: Receive = {
    case Constants.CLEANER_MESSAGE_TYPE => {
      cleanAll()
    }
  }

  def cleanAll() = {
    redis.smembers[String](Constants.TENANT_SET).onComplete { tenants =>
      tenants.get.foreach { tenant =>
        clean(tenant)
      }
    }
  }

  def clean(tenantId: String): Unit = {
 		log.info(s"[${format.format(new Date)}](${CleanerActor.getClass})clean redis expired data, tenantId: ${tenantId}")
    redis.smembers[String](Constants.IP_PRE + tenantId).onComplete { ownids =>
      ownids.get.foreach { ownid =>
        val tb = s"tb_${tenantId}_${ownid}"
        val currentTime = System.currentTimeMillis()
        doClean(tb, currentTime)
      }
    }
  }

  def doClean(tb: String, ts: Long): Unit = {
    // 2 * 3600 * 1000 ms = 2 hours + 30000（30s的修正值）
    redis.zremrangebyscore(tb, new Limit(0, true), new Limit(ts - 7230000, true))
  }
}
