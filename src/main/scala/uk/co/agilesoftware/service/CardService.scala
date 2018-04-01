package uk.co.agilesoftware.service

import uk.co.agilesoftware.connector.{ CSCardsConnector, CardsConnector, ScoredCardsConnector }
import uk.co.agilesoftware.domain.{ Applicant, Card }

import scala.concurrent.{ ExecutionContext, Future }

trait CardService {
  protected def cscardsConnector: CardsConnector
  protected def scoredCardsConnector: CardsConnector

  def getCards(applicant: Applicant)(implicit ec: ExecutionContext): Future[Seq[Card]] = {
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
  override protected val cscardsConnector: CardsConnector = CSCardsConnector
  override protected val scoredCardsConnector: CardsConnector = ScoredCardsConnector
}