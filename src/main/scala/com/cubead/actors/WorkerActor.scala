package com.cubead.actors

import akka.actor.{Props, ActorLogging, Actor}
import akka.actor.Actor.Receive
import com.cubead.Application
import com.cubead.conf.Constants
import com.cubead.model.{WorkResult, LogItem}
import com.cubead.utils.CacheManager
import redis.api.Limit
import redis.{RedisClient, RedisServer}
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID

/**
 * Created by xiaoao on 5/18/15.
 */
object WorkerActor{
  def props(redis: RedisClient): Props = Props(new WorkerActor(redis))
}

class WorkerActor(redis: RedisClient) extends Actor with ActorLogging {
  val transaction = redis.transaction()
  val resultActor = context.actorOf(Props(classOf[ResultActor], Application.redis), "result-actor")

  override def receive: Receive = {
    case message: LogItem =>
      if (!message.check) {
        val conf = CacheManager.getConf(message.tenantId)
        if (!conf.equals(Nil)) {
          val tb = message.toTBName //s"tb_${message.tenantId}_${message.onuid}"

          // add totle
          redis.sadd(Constants.TENANT_SET, message.tenantId)
          redis.sadd(Constants.IP_PRE + message.tenantId, message.onuid)

          transaction.watch(tb)
          val time = message.timestamp.toLong.toString
          val uUID = s"${time}_${UUID.randomUUID().toString}"
          transaction.zadd(tb, (message.timestamp, uUID))
          println(s"receive msg: ${message.onuid}")

          val steps = conf.rule.sliding(2, 2)
          val results = for {
            step <- steps
          } yield {
              val duration = message.timestamp - step.head
              val count = transaction.zcount(tb, new Limit(duration, true), new Limit(message.timestamp, true))
              step -> count
            }

          transaction.exec()

          results.foreach { r =>
            r._2.onComplete { c =>
              val count = c.get
              if (count >= r._1(1)) {
                val wr = WorkResult(message.tenantId, message.ip, message.onuid, message.timestamp, r._1.head)
                resultActor ! wr
              }
            }
          }
        }
      }
  }
}
