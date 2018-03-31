package uk.co.agilesoftware.connector

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{Matchers, WordSpec}
import uk.co.agilesoftware.domain.{Applicant, Card, EmploymentStatus}

class CSCardsConnectorSpec extends WordSpec with Matchers with WiremockSpec with ScalaFutures with IntegrationPatience {

  val connector = new CSCardsConnector {
    override implicit def url: String = s"$wiremockUrl/v1/cards"
  }

  "connector" should {
    "be able to fetch cs cards" in {
      stubFor(post(urlPathEqualTo("/v1/cards"))
        .withRequestBody(equalToJson("""{"fullName": "saurabh arora", "dateOfBirth": "1980/07/03", "creditScore": 500}"""))
        .willReturn(
        aResponse()
          .withStatus(200)
          .withHeader("Content-Type", "application/json")
          .withBody(
            s"""[
               |{"cardName": "SuperSaver Card", "url": "http://www.example.com/apply", "apr": 21.4, "eligibility": 6.3},
               |{"cardName": "SuperSpender Card", "url": "http://www.example.com/apply", "apr": 19.2, "eligibility": 5.0, "features": ["featureOne"]}
               |]""".stripMargin)
      ))

       whenReady(connector.getCards(Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 100000))) { cards =>
         cards should contain theSameElementsAs Seq(
           Card("CSCards", "SuperSaver Card", "http://www.example.com/apply", 21.4, 6.3),
           Card("CSCards", "SuperSpender Card", "http://www.example.com/apply", 19.2, 5.0, Seq("featureOne"))
         )
       }
    }
  }

  "request body" should {
    "be proper json" in {
      import spray.json._
      val requestBody = CSCardsConnector.requestBody(Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 100000))
      requestBody.parseJson shouldBe s"""{"fullName": "saurabh arora", "dateOfBirth": "1980/07/03", "creditScore": 500}""".parseJson
    }
  }
}
