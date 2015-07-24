package com.cubead.actors

import akka.actor.{ActorLogging, Actor}
import akka.actor.Actor.Receive
import com.cubead.http.HitService

/**
 * Created by xiaoao on 5/18/15.
 */
class HttpServiceActor extends Actor with HitService with ActorLogging{
  def actorRefFactory = context
  def receive = runRoute(sprayRoute)
}
