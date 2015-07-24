package com.cubead.http

import com.cubead.actors.CacheActor
import com.cubead.model.Conf
import akka.pattern.ask
import scala.concurrent.duration._
import akka.actor.Props
import akka.util.Timeout
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.{ read, write }
import spray.can.Http
import spray.httpx.Json4sSupport
import spray.routing._
import spray.can.server.Stats
import spray.http.StatusCodes._
/**
 * Created by xiaoao on 5/18/15.
 */
object Json4sProtocol extends Json4sSupport {
  implicit def json4sFormats: Formats = DefaultFormats
}

trait HitService extends HttpService{
  import Json4sProtocol._
  import CacheActor._

  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)

  val cacheActor = actorRefFactory.actorOf(Props[CacheActor], "cache-actor")
  val sprayRoute = {
    path("entity") {
        post {
          respondWithStatus(Created) {
            entity(as[Conf]) { someObject =>
              doCreate(someObject)
            }
          }
        }
    } ~
      path("stats") {
        complete {
          actorRefFactory.actorSelection("/user/IO-HTTP/listener-0")
            .ask(Http.GetStats)(1.second)
            .mapTo[Stats]
        }
      }
  }

  def doCreate[T](conf: Conf) = {
    complete {
      (cacheActor ? Create(conf))
        .mapTo[Ok]
        .map(result => s"updating cache info: ${result}")
        .recover { case _ => "error" }
    }
  }
}
