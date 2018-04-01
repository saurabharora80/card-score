package uk.co.agilesoftware.connector

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object TestSingletons {
  private[connector] implicit val system: ActorSystem = ActorSystem("testActorSystem")
  private[connector] implicit val materializer: ActorMaterializer = ActorMaterializer()
}
