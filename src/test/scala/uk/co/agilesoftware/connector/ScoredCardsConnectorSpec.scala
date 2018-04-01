package uk.co.agilesoftware.connector

import akka.http.scaladsl.model.StatusCodes
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ Matchers, WordSpec }
import uk.co.agilesoftware.domain.{ Applicant, Card, EmploymentStatus }

class ScoredCardsConnectorSpec extends WordSpec with Matchers with WiremockSpec with ScalaFutures with IntegrationPatience {

  import uk.co.agilesoftware.TestSingletons._

  private val connector = new ScoredCardsConnector {
    override protected def url: String = s"$wiremockUrl/v2/creditcards"
  }

  "connector" should {
    "be able to fetch scored cards" in {
      val applicant = Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 10000)

      import uk.co.agilesoftware.WiremockStub._

      given(post(urlPathEqualTo("/v2/creditcards")).withRequestBody(equalToJson(ScoredCardsConnector.requestBody(applicant))))
        .returns(Seq(
          scoredCardsResponse("ScoredCard Builder", "http://www.example.com/apply", 21.4, 0.8),
          scoredCardsResponse("ScoredCard Builder2", "http://www.example.com/apply", 18.4, 0.7)
        ))

      whenReady(connector.getCards(applicant)) { cards =>
        cards should contain theSameElementsAs Seq(
          Card("ScoredCards", "ScoredCard Builder", "http://www.example.com/apply", 21.4, 8.0),
          Card("ScoredCards", "ScoredCard Builder2", "http://www.example.com/apply", 18.4, 7.0)
        )
      }
    }

    "be able to handle empty scored cards response" in {
      val applicant = Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 10000)

      import uk.co.agilesoftware.WiremockStub._

      given(post(urlPathEqualTo("/v2/creditcards")).withRequestBody(equalToJson(ScoredCardsConnector.requestBody(applicant))))
        .returns(Seq())

      whenReady(connector.getCards(applicant)) { _ shouldBe empty }
    }

    "return empty list if scored cards is unreachable" in {
      import uk.co.agilesoftware.WiremockStub._

      given(post(urlPathEqualTo("/v2/creditcards"))).failsWith(StatusCodes.ServiceUnavailable)

      whenReady(connector.getCards(Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 10000))) { _ shouldBe empty }
    }

    "return empty list if scored cards response is missing mandatory field" in {
      import uk.co.agilesoftware.WiremockStub._

      given(post(urlPathEqualTo("/v2/creditcards")))
        .returns(Seq("""{"first-name": "saurabh", "date-of-birth": "1980/07/03",
                          "score": 500, "employment-status": "STUDENT", "salary": 100000}"""))

      whenReady(connector.getCards(Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 10000))) { _ shouldBe empty }
    }
  }

  "request body" should {
    "be correct json" in {
      import spray.json._
      val cardRequestBody = ScoredCardsConnector.requestBody(Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 100000))

      cardRequestBody.parseJson shouldBe
        s"""{"first-name": "saurabh", "last-name": "arora", "date-of-birth": "1980/07/03",
            "score": 500, "employment-status": "STUDENT", "salary": 100000}""".parseJson
    }
  }

}
