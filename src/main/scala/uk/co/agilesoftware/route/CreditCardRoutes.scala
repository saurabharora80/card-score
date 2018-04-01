package uk.co.agilesoftware.route

import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import uk.co.agilesoftware.service.CardService
import uk.co.agilesoftware.{ Singletons, domain }

import scala.concurrent.duration._

trait CreditCardRoutes extends DefaultRejectionHandler {
  import Singletons._

  lazy val log = Logging(system, classOf[CreditCardRoutes])

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  val cardService: CardService

  import JsonWriters._

  lazy val creditCardRoutes: Route = path("creditcards") {
    (post & entity(as[domain.Applicant])) { applicant =>
      onSuccess(cardService.getCards(applicant)) { cards =>
        complete(cards.sorted)
      }
    }
  }

}

