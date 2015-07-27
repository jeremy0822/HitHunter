package com.cubead.actors

import java.nio.charset.Charset

import akka.actor.{ActorLogging, Props, Actor}
import akka.actor.Actor.Receive
import com.cubead.conf.Constants
import com.cubead.utils.KafkaUtil
import kafka.api.FetchRequestBuilder
import kafka.common.ErrorMapping
import kafka.consumer.SimpleConsumer

/**
 * Created by xiaoao on 5/18/15.
 */

object CAKafkaConsumer{
  def props(topic: String): Props = Props(new CAKafkaConsumer(topic))
}

class CAKafkaConsumer(topic: String) extends Actor with ActorLogging{

  val parser = context.actorOf(Props[ParserActor],"parser")
  var charset = Charset.forName("UTF-8")
  var decoder = charset.newDecoder()

  val consumer = new SimpleConsumer(Constants.kAFKA_HOST, Constants.kAFKA_PORT, Constants.kAFKA_TIMEOUT, Constants.kAFKA_BUFFER, Constants.KAFKA_CLIENT_ID)
  override def receive: Receive =  {
    case info: String =>
      var OFFSET = KafkaUtil.getLastOffset
      if(OFFSET > Constants.kAFKA_OFFSET){
        OFFSET = Constants.kAFKA_OFFSET
      }
        println(s"-----------------offset: ${OFFSET}")

        val req = new FetchRequestBuilder()
          .clientId(Constants.KAFKA_CLIENT_ID)
          .addFetch(topic, 0, OFFSET, 100000)
          .build();
        val fetchResponse = consumer.fetch(req)
        if (fetchResponse.hasError) {
          val code = fetchResponse.errorCode(topic, 0)
          if (code == ErrorMapping.OffsetOutOfRangeCode) {
            println("----------")

          }
        } else {
          fetchResponse.messageSet(topic, 0).foreach { msg =>
            val payload = msg.message.payload
            val of = msg.nextOffset
            val currentOffset = msg.offset
            if (currentOffset >= OFFSET) {
              val info = decoder.decode(payload).toString
//              println("msg:{}---", info)
              parser ! info
              Constants.kAFKA_OFFSET = of
            }else{
              println("error ... ")
            }
          }
        }

  }

}
