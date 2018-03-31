package uk.co.agilesoftware.service

import uk.co.agilesoftware.connector.{CSCardsConnector, CardsConnector, ScoredCardsConnector}
import uk.co.agilesoftware.domain.{Applicant, CreditCard}

import scala.concurrent.{ExecutionContext, Future}

trait CardService {
  def cscardsConnector: CardsConnector
  def scoredCardsConnector: CardsConnector

  def getCards(applicant: Applicant)(implicit ec: ExecutionContext): Future[Seq[CreditCard]] = {
    val cards = for {
      cscards <- cscardsConnector.getCards(applicant)
      scoredCards <- scoredCardsConnector.getCards(applicant)
    } yield cscards ++ scoredCards

    cards.map { cards =>
      cards.map(_ => CreditCard("", "", "", 0.0, Seq(""), 0.0))
    }
  }
}

object CardService extends CardService {
  override val cscardsConnector: CardsConnector = CSCardsConnector
  override val scoredCardsConnector: CardsConnector = ScoredCardsConnector
}