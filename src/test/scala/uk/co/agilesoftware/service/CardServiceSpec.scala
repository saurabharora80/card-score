package uk.co.agilesoftware.service

import org.mockito.BDDMockito.given
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{ FlatSpec, Matchers }
import uk.co.agilesoftware.connector.CardsConnector
import uk.co.agilesoftware.domain.{ Applicant, Card, EmploymentStatus }

import scala.concurrent.Future

class CardServiceSpec extends FlatSpec with Matchers with MockitoSugar with ScalaFutures with IntegrationPatience {

  private val mockCSCardsConnector = mock[CardsConnector]
  private val mockScoredCardsConnector = mock[CardsConnector]

  import uk.co.agilesoftware.TestSingletons._

  private val service = new CardService {
    override def cscardsConnector: CardsConnector = mockCSCardsConnector
    override def scoredCardsConnector: CardsConnector = mockScoredCardsConnector
  }

  "service" should "return a combined list of CSCards and ScoredCards" in {

    val applicant = Applicant("saurabh", "arora", "1980/10/10", 500, EmploymentStatus.FULL_TIME, 10000)

    val csCard = Card("CSCards", "cardOne", "http://www.example.com", 19.4, 6.3)
    given(mockCSCardsConnector.getCards(applicant)).willReturn(Future.successful(Seq(csCard)))

    val scoredCard = Card("ScoredCards", "cardTwo", "http://www.example.com", 21.4, 5.0)
    given(mockScoredCardsConnector.getCards(applicant)).willReturn(Future.successful(Seq(scoredCard)))

    whenReady(service.getCards(applicant)) { cards =>
      cards should contain theSameElementsAs Seq(csCard, scoredCard)
    }
  }
}
