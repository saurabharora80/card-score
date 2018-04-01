package uk.co.agilesoftware

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import uk.co.agilesoftware.route.CreditCardRoutes
import uk.co.agilesoftware.service.CardService

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object QuickstartServer extends App with CreditCardRoutes {

  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  override val cardService = CardService

  Http().bindAndHandle(creditCardRoutes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)

}
