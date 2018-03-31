package uk.co.agilesoftware

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import spray.json.DefaultJsonProtocol._
import uk.co.agilesoftware.domain.CreditCard
import uk.co.agilesoftware.service.CardService

import scala.concurrent.duration._

trait CreditCardRoutes extends DefaultRejectionHandler {
  implicit def system: ActorSystem
  private implicit val executionContext = system.dispatcher

  lazy val log = Logging(system, classOf[CreditCardRoutes])

  implicit val cardFormat = jsonFormat6(CreditCard)

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  val cardService: CardService

  lazy val creditCardRoutes: Route = path("creditcards") {
    (post & entity(as[domain.Applicant])) { applicant =>
      onSuccess(cardService.getCards(applicant)) { cards =>
        complete(cards)
      }
    }
  }

}

