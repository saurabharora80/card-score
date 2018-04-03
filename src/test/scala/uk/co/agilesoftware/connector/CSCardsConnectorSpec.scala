package uk.co.agilesoftware.connector

import akka.http.scaladsl.model.StatusCodes
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ Matchers, WordSpec }
import uk.co.agilesoftware.WiremockSpec
import uk.co.agilesoftware.domain.{ Applicant, Card, EmploymentStatus }

class CSCardsConnectorSpec extends WordSpec with Matchers with WiremockSpec with ScalaFutures with IntegrationPatience {

  import uk.co.agilesoftware.TestSingletons._

  private val connector = new CSCardsConnector {
    override implicit def url: String = s"$wiremockUrl/v1/cards"
  }

  "connector" should {
    "be able to fetch cs cards" in {

      val applicant = Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 100000)

      import uk.co.agilesoftware.WiremockStub._

      given(post(urlPathEqualTo("/v1/cards")).withRequestBody(equalToJson(CSCardsConnector.requestBody(applicant))))
        .returns(Seq(
          csCardsResponse("SuperSaver Card", "http://www.example.com/apply", 21.4, 6.3),
          csCardsResponse("SuperSpender Card", "http://www.example.com/apply", 19.2, 5.0, "featureOne")
        ))

      whenReady(connector.getCards(applicant)) { cards =>
        cards should contain theSameElementsAs Seq(
          Card("CSCards", "SuperSaver Card", "http://www.example.com/apply", 21.4, 6.3),
          Card("CSCards", "SuperSpender Card", "http://www.example.com/apply", 19.2, 5.0, Seq("featureOne"))
        )
      }
    }

    "be able to handle empty CS Cards response" in {
      val applicant = Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 10000)

      import uk.co.agilesoftware.WiremockStub._

      given(post(urlPathEqualTo("/v1/cards")).withRequestBody(equalToJson(CSCardsConnector.requestBody(applicant))))
        .returns(Seq())

      whenReady(connector.getCards(applicant)) { _ shouldBe empty }
    }

    "return empty list if scored cards is unreachable" in {
      import uk.co.agilesoftware.WiremockStub._

      given(post(urlPathEqualTo("/v1/cards"))).failsWith(StatusCodes.ServiceUnavailable)

      whenReady(connector.getCards(Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 10000))) { _ shouldBe empty }
    }

    "return empty list if scored cards response is missing mandatory field" in {
      import uk.co.agilesoftware.WiremockStub._

      given(post(urlPathEqualTo("/v1/cards")))
        .returns(Seq("""{"fullName": "saurabh arora", "dateOfBirth": "1980/07/03"}"""))

      whenReady(connector.getCards(Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 10000))) { _ shouldBe empty }
    }

    "return an empty list if the connection times out" in {
      import uk.co.agilesoftware.WiremockStub._
      import scala.concurrent.duration._

      implicit val delay: Duration = 7.seconds

      given(post(urlPathEqualTo("/v1/cards")))
        .returns(Seq(csCardsResponse("SuperSaver Card", "http://www.example.com/apply", 21.4, 6.3)))

      whenReady(connector.getCards(Applicant("saurabh", "arora", "1980/07/03", 500, EmploymentStatus.STUDENT, 100000))) { _ shouldBe empty }
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
