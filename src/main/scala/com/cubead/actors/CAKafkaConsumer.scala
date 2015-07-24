package com.cubead.actors

import java.nio.charset.Charset
import akka.actor.{ ActorLogging, Props, Actor }
import akka.actor.Actor.Receive
import com.cubead.conf.Constants
import com.cubead.utils.KafkaUtil
import kafka.api.FetchRequestBuilder
import kafka.common.ErrorMapping
import kafka.consumer.SimpleConsumer
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Created by xiaoao on 5/18/15.
 */

object CAKafkaConsumer {
  def props(topic: String): Props = Props(new CAKafkaConsumer(topic))
}

class CAKafkaConsumer(topic: String) extends Actor with ActorLogging {

  val parser = context.actorOf(Props[ParserActor], "parser")
  var charset = Charset.forName("UTF-8")
  var decoder = charset.newDecoder()
  val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  val consumer = new SimpleConsumer(Constants.kAFKA_HOST, Constants.kAFKA_PORT, Constants.kAFKA_TIMEOUT, Constants.kAFKA_BUFFER, Constants.KAFKA_CLIENT_ID)
  override def receive: Receive = {
    case info: String =>
      var OFFSET = KafkaUtil.getLastOffset
      println(s"[${format.format(new Date())}](${CAKafkaConsumer.getClass}) begin consume data from kafka,lastOffset: ${OFFSET},currentOffset:${Constants.kAFKA_OFFSET}")
      
      if (OFFSET > Constants.kAFKA_OFFSET) {
        OFFSET = Constants.kAFKA_OFFSET
      } else if(OFFSET <= Constants.kAFKA_OFFSET) {
        Constants.kAFKA_OFFSET = OFFSET
      }
      
      val req = new FetchRequestBuilder()
        .clientId(Constants.KAFKA_CLIENT_ID)
        .addFetch(topic, 0, OFFSET, 1000000)
        .build();
      val fetchResponse = consumer.fetch(req)
      if (fetchResponse.hasError) {
        val code = fetchResponse.errorCode(topic, 0)
        if (code == ErrorMapping.OffsetOutOfRangeCode) {
          println(s"[${format.format(new Date())}](${CAKafkaConsumer.getClass}) read data from kafka error:${code}")

        }
      } else {
        fetchResponse.messageSet(topic, 0).foreach { msg =>
          val payload = msg.message.payload
          val of = msg.nextOffset
          val currentOffset = msg.offset
          if (currentOffset >= OFFSET) {
            val info = decoder.decode(payload).toString
            parser ! info
            Constants.kAFKA_OFFSET = of
          } else {
            println(s"[${format.format(new Date())}](${CAKafkaConsumer.getClass}) read data from kafka error,currentOffset:${currentOffset},offset:${KafkaUtil.getLastOffset} which have been readed.")
          }
        }
      }
      println(s"[${format.format(new Date())}](${CAKafkaConsumer.getClass}) end consume data from kafka,lastOffset: ${KafkaUtil.getLastOffset},currentOffset:${Constants.kAFKA_OFFSET}")
  }

}
