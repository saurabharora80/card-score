package uk.co.agilesoftware.service

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import uk.co.agilesoftware.connector.{ CSCardsConnector, CardsConnector, ScoredCardsConnector }
import uk.co.agilesoftware.domain.{ Applicant, Card }

import scala.concurrent.{ ExecutionContext, Future }

trait CardService {
  def cscardsConnector: CardsConnector
  def scoredCardsConnector: CardsConnector

  def getCards(applicant: Applicant)(implicit ec: ExecutionContext, actorSystem: ActorSystem, materializer: ActorMaterializer): Future[Seq[Card]] = {
    //Initialise the futures outside 'for' comprehension to allow parallel execution
    val eventualCSCards = cscardsConnector.getCards(applicant)
    val eventualScoredCards = scoredCardsConnector.getCards(applicant)
    for {
      cscards <- eventualCSCards
      scoredCards <- eventualScoredCards
    } yield cscards ++ scoredCards
  }
}

object CardService extends CardService {
  override val cscardsConnector: CardsConnector = CSCardsConnector
  override val scoredCardsConnector: CardsConnector = ScoredCardsConnector
}