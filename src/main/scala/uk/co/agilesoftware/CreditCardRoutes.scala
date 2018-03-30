package uk.co.agilesoftware

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.util.Timeout
import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

trait CreditCardRoutes extends JsonSupport with DefaultRejectionHandler {
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[CreditCardRoutes])

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val creditCardRoutes: Route = path("creditcards") {
    (post & entity(as[domain.Applicant])) { _ =>
      complete(StatusCodes.OK)
    }
  }

}
