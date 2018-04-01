package uk.co.agilesoftware

import akka.http.scaladsl.Http
import uk.co.agilesoftware.route.CreditCardRoutes
import uk.co.agilesoftware.service.CardService

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object QuickstartServer extends App with CreditCardRoutes {

  import Singletons._
  override val cardService = CardService

  Http().bindAndHandle(creditCardRoutes, Config.host, Config.port)

  println(s"Server online at http://${Config.host}:${Config.port}/")

  Await.result(system.whenTerminated, Duration.Inf)

}
