package uk.co.agilesoftware.connector

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{Matchers, WordSpec}
import uk.co.agilesoftware.domain.{Applicant, Card, EmploymentStatus}

class ScoredCardsConnectorSpec extends WordSpec with Matchers with WiremockSpec with ScalaFutures with IntegrationPatience {

  private val connector = new ScoredCardsConnector {
    override protected def url: String = s"$wiremockUrl/v2/creditcards"
  }

  "connector" should {
    "be able to fetch cs cards" in {
      stubFor(post(urlPathEqualTo("/v2/creditcards"))
        .withRequestBody(equalToJson(
          """{"first-name": "saurabh", "last-name": "arora", "date-of-birth": "1980/07/03",
            |"score": 500, "employment-status": "STUDENT", "salary": 100000}""".stripMargin))
        .willReturn(
        aResponse()
          .withStatus(200)
          .withHeader("Content-Type", "application/json")
          .withBody(
            s"""[
               |{"card": "ScoredCard Builder", "apply-url": "http://www.example.com/apply", "annual-percentage-rate": 21.4,
               |"approval-rating": 0.8},
               |{"card": "ScoredCard Builder3", "apply-url": "http://www.example.com/apply", "annual-percentage-rate": 18.4,
               |"approval-rating": 2.8, "attributes": []},
               |{"card": "ScoredCard Builder2", "apply-url": "http://www.example.com/apply", "annual-percentage-rate": 19.4,
               |"approval-rating": 1.8, "attributes": ["Supports ApplePay"], "introductory-offers": ["Interest free for 1 month"]}
               |]""".stripMargin)
      ))

       whenReady(connector.getCards(Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 100000))) { cards =>
         cards should contain theSameElementsAs Seq(
           Card("ScoredCards", "ScoredCard Builder", "http://www.example.com/apply", 21.4, 0.8),
           Card("ScoredCards", "ScoredCard Builder3", "http://www.example.com/apply", 18.4, 2.8),
           Card("ScoredCards", "ScoredCard Builder2", "http://www.example.com/apply", 19.4, 1.8, Seq("Supports ApplePay", "Interest free for 1 month"))
         )
       }
    }
  }

  "request body" should {
    "be correct json" in {
      import spray.json._
      val cardRequestBody = ScoredCardsConnector.requestBody(Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 100000))

      cardRequestBody.parseJson shouldBe s"""{"first-name": "saurabh", "last-name": "arora", "date-of-birth": "1980/07/03",
                                                "score": 500, "employment-status": "STUDENT", "salary": 100000}""".parseJson
    }
  }
}
