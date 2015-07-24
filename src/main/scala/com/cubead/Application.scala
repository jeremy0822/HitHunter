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
  val appHost = system.settings.config.getString("app.host")
  val appPort = system.settings.config.getInt("app.port")
  
  Constants.kAFKA_HOST = system.settings.config.getString("kafka.connection")
  Constants.kAFKA_PORT = system.settings.config.getInt("kafka.port")
  Constants.kAFKA_ZOOKEEPER = system.settings.config.getString("kafka.zookeeper")
  Constants.kAFKA_GROUPID = system.settings.config.getString("kafka.groupId")
  Constants.kAFKA_TOPIC = system.settings.config.getString("kafka.topic")
  
  val redisHost = system.settings.config.getString("redis.host")
  val redisPort = system.settings.config.getInt("redis.port")
  val redis = RedisClient(host = redisHost, port = redisPort)

  val service = system.actorOf(Props[HttpServiceActor], "spray-http-service")
  IO(Http) ! Http.Bind(service, appHost, appPort)

  val cacheActor = system.actorOf(Props[CacheActor], "cache-actor")
  cacheActor ! Constants.CACHE_UPDATE_ALL

//the value in list are couples of k-v (ms,times),for test
//  val rule = system.settings.config.getString("app.rule")
//  val tenantId = system.settings.config.getString("app.tenantId")
//  val rules = rule.split(",").toList.map { _.toInt}
//  val conf = new Conf(tenantId, rules)
//  CacheManager.setConf(List(conf))

  // clean log data
  val cleanActor = system.actorOf(Props(classOf[CleanerActor], redis), "cleaner-actor")
  system.scheduler.schedule(Duration.create(0, TimeUnit.MILLISECONDS), Duration.create(cleanerInterval, TimeUnit.SECONDS), cleanActor, Constants.CLEANER_MESSAGE_TYPE)

  // read data from kafka last offset
  //-3000for test
  Constants.kAFKA_OFFSET = KafkaUtil.getLastOffset()
  val kafkaActor = system.actorOf(Props(classOf[CAKafkaConsumer], Constants.kAFKA_TOPIC), "kafka-consumer")
  val kafkaReadInterval = system.settings.config.getInt("app.kafka-read-interval")
  system.scheduler.schedule(Duration.create(0, TimeUnit.MILLISECONDS), Duration.create(kafkaReadInterval, TimeUnit.SECONDS), kafkaActor, Constants.kAFKA_START_CMD)
}
