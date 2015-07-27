package com.cubead

import java.util.concurrent.TimeUnit

import _root_.kafka.consumer.SimpleConsumer
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import com.cubead.actors._
import com.cubead.conf.Constants
import com.cubead.model.Conf
import com.cubead.utils.{CacheManager, KafkaUtil}
import redis.RedisClient
import spray.can.Http
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends App{
  implicit val system = akka.actor.ActorSystem("cubead-hithunter")
  Constants.CACHE_SOURCE_URL = system.settings.config.getString("app.hit-conf-url")
  val cleanerInterval = system.settings.config.getInt("app.clean-duration")

  val host = system.settings.config.getString("redis.host")
  val port = system.settings.config.getInt("redis.port")
//  val redisServe = RedisServer(host = host, port = port)
//  val redis = RedisClientPool(redisServe, "redis-server-pool")
  val redis = RedisClient(host = host, port = port)

  val service = system.actorOf(Props[HttpServiceActor], "spray-http-service")
  IO(Http) ! Http.Bind(service, system.settings.config.getString("app.host"), system.settings.config.getInt("app.port"))

//  val worker = system.actorOf(Props(classOf[WorkerActor], redis), "worker-actor")

//  val updateCache = system.settings.config.getInt("app.update-cache-interval")
  val cacheActor = system.actorOf(Props[CacheActor], "cache-actor")
  cacheActor ! Constants.CACHE_UPDATE_ALL

//  system.scheduler.schedule(Duration.create(10, TimeUnit.MILLISECONDS), Duration.create(updateCache, TimeUnit.SECONDS), cacheActor, Constants.CACHE_UPDATE_ALL)

  // sleep 30s to wait get all cache data
//  Thread.sleep(30000)
  val conf = new Conf("119378", List(2,2,10,5,100,10))
  CacheManager.setConf(List(conf))

  // clean log data
  val cleanActor = system.actorOf(Props(classOf[CleanerActor], redis), "cleaner-actor")
  system.scheduler.schedule(Duration.create(0, TimeUnit.MILLISECONDS), Duration.create(cleanerInterval, TimeUnit.SECONDS), cleanActor, Constants.CLEANER_MESSAGE)

  // read data from kafka
  //val (zookepers, brockers, bport, groupId, topic) = (, ,  , , ) //("192.168.3.102:2181", "192.168.3.102:9092", "grtca")
  Constants.kAFKA_HOST = system.settings.config.getString("kafka.connection")
  Constants.kAFKA_PORT = system.settings.config.getInt("kafka.port")
  Constants.kAFKA_ZOOKEEPER = system.settings.config.getString("kafka.zookeeper")
  Constants.kAFKA_GROUPID = system.settings.config.getString("kafka.groupId")
  Constants.kAFKA_TOPIC = system.settings.config.getString("kafka.topic")

  val consumer = new SimpleConsumer(Constants.kAFKA_HOST, Constants.kAFKA_PORT,100000,64*1024, Constants.KAFKA_CLIENT_ID)
//  val offset = KafkaUtil.getLastOffset(Constants.kAFKA_HOST, Constants.kAFKA_PORT,100000,64*1024, Constants.kAFKA_TOPIC, 0, System.currentTimeMillis(),Constants.KAFKA_CLIENT_ID) - 2
//  val offset = KafkaUtil.getLastOffset()
  Constants.kAFKA_OFFSET = KafkaUtil.getLastOffset() - 300
  val kafkaActor = system.actorOf(Props(classOf[CAKafkaConsumer], Constants.kAFKA_TOPIC), "kafka-consumer")

  val kafkaReadInterval = system.settings.config.getInt("app.kafka-read-interval")
  system.scheduler.schedule(Duration.create(0, TimeUnit.MILLISECONDS), Duration.create(kafkaReadInterval, TimeUnit.SECONDS), kafkaActor, Constants.kAFKA_START_CMD)
}
