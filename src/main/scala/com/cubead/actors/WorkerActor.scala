package com.cubead.actors

import akka.actor.{ Props, ActorLogging, Actor }
import akka.actor.Actor.Receive
import com.cubead.Application
import com.cubead.conf.Constants
import com.cubead.model.{ WorkResult, LogItem }
import com.cubead.utils.CacheManager
import com.cubead.model.Conf
import redis.api.Limit
import redis.{ RedisClient, RedisServer }
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Created by xiaoao on 5/18/15.
 */
object WorkerActor {
  def props(redis: RedisClient): Props = Props(new WorkerActor(redis))
}

class WorkerActor(redis: RedisClient) extends Actor with ActorLogging {
  val transaction = redis.transaction()
  val resultActor = context.actorOf(Props(classOf[ResultActor], Application.redis), "result-actor")
  val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  override def receive: Receive = {
    case message: LogItem =>
      val startTime: Long = System.currentTimeMillis()
      if (!message.check) {
        val conf:Option[Conf]= CacheManager.getConf(message.tenantId)
        conf match {
          case Some(content) => {
            val tb = message.toTBName

            // add totle
            redis.sadd(Constants.TENANT_SET, message.tenantId)
            redis.sadd(Constants.IP_PRE + message.tenantId, message.onuid)

            transaction.watch(tb)
            val time = message.timestamp.toLong.toString
            val uUID = s"${time}_${UUID.randomUUID().toString}"
            transaction.zadd(tb, (message.timestamp, uUID))

            val steps = content.toSteps
            val results = for {
              step <- steps
            } yield {
              val duration = message.timestamp - step.head.*(1000)
              val count = transaction.zcount(tb, new Limit(duration, true), new Limit(message.timestamp, true))
              step -> count
            }
            //result变成新的映射例如(list(2,2),3)

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
            println(s"[${format.format(new Date)}](${WorkerActor.getClass})receive msg: (tenantId:${message.tenantId},OWN_UID:${message.onuid},IP:${message.ip},cost ${(System.currentTimeMillis() - startTime)} ms.")
          }
          case None => {
            println(s"[${format.format(new Date)}](${WorkerActor.getClass})${message.tenantId} rule not found.")
          }
        }
      }
  }
}
