package uk.co.agilesoftware

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object TestSingletons {
  implicit val system: ActorSystem = ActorSystem("testActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit lazy val executionContext = system.dispatcher
}
