package com.cubead.actors

import akka.actor.{Props, ActorLogging, Actor}
import com.cubead.Application
import com.cubead.conf.Constants
import com.cubead.model.LogItem

import scala.collection.mutable

/**
 * Created by xiaoao on 5/18/15.
 */
class ParserActor extends Actor with ActorLogging{
  val worker = context.actorOf(Props(classOf[WorkerActor], Application.redis), "worker-actor")
  override def receive: Receive = {
    case message: String =>
      val data = message.split("\\]\\[")
      val itemMap = mutable.HashMap[String, String]()

      // filtes the data from baidu

      data.map{ msg =>
        if(msg.startsWith(Constants.Timestamp)){
          val a = msg.split(":").last.trim
          println(s"Timestamp:${a}-")
          itemMap += (Constants.Timestamp -> a)
        }
        if(msg.startsWith(Constants.Cookie)){
          val b = msg.split(";").filter(_.startsWith(Constants.OWN_UID))(0).substring(8)
//          println(s"OWN_UID:${b}-")
          itemMap += (Constants.OWN_UID -> b)
        }
        if(msg.startsWith(Constants.Browser)){
          val index1 = msg.indexOf(":") + 2
          val index2 = msg.indexOf("-") - 1
          val c = msg.substring(index1, index2)
//          println(s"Browser:${c}-")
          itemMap += (Constants.Browser -> c)
        }
        if(msg.startsWith(Constants.Request)){
          val d = msg.split("&").filter(_.startsWith(Constants.Tenant))(0).substring(Constants.Tenant.length + 1)
          println(s"Tenant:${d}-")
          itemMap += (Constants.Tenant -> d)
        }
      }
      val item = LogItem(itemMap.get(Constants.Tenant).getOrElse("0"), itemMap.get(Constants.Browser).getOrElse("0"), itemMap.get(Constants.OWN_UID).getOrElse("0"), itemMap.get(Constants.Timestamp).getOrElse("0").toDouble)

//      println(s"parse message: ${info}")
      worker ! item
  }
}
