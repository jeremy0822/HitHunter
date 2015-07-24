package com.cubead.actors

import akka.actor.{ActorLogging, Actor}
import akka.actor.Actor.Receive
import com.cubead.conf.Constants
import com.cubead.model.Conf
import com.cubead.utils.{CacheManager, CacheBuilderHelper}

/**
 * Created by xiaoao on 5/18/15.
 */
object CacheActor {
  case class Ok(info: String)
  case class Create(conf: Conf)
}
class CacheActor extends Actor with ActorLogging{
  import CacheActor._

  override def receive: Receive = {
    case Constants.CACHE_UPDATE_ALL =>
        CacheBuilderHelper.buildCache()
    case Create(conf) => {
      log.info(s"Update cache: ${conf.tid} to ${conf.rule}")
      CacheManager.setConf(List(conf))
      sender ! Ok("updated")
    }
  }
}
