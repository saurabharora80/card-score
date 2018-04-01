package uk.co.agilesoftware

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.tomakehurst.wiremock.client.WireMock.{post, urlPathEqualTo}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import spray.json.{DefaultJsonProtocol, JsArray, JsNumber, JsObject, JsString, JsValue}
import uk.co.agilesoftware.connector.{CSCardsConnector, CardsConnector, ScoredCardsConnector, WiremockSpec}
import uk.co.agilesoftware.domain.EmploymentStatus
import uk.co.agilesoftware.service.CardService

class CreditCardRouteSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with CreditCardRoutes with WiremockSpec {

  override val cardService: CardService = new CardService {
    override def cscardsConnector: CardsConnector = new CSCardsConnector {
      override protected def url: String = s"$wiremockUrl/v1/cards"
    }
    override def scoredCardsConnector: CardsConnector = new ScoredCardsConnector {
      override protected def url: String = s"$wiremockUrl/v2/creditcards"
    }
  }

  val httpEntity: (String) => HttpEntity.Strict = (str: String) => HttpEntity(ContentTypes.`application/json`, str)

  import DefaultJsonProtocol._
  private implicit val errorFormat = jsonFormat3(ValidationError)

  "client" should {
    "not be able to request credit card with empty body" in {
      Post("/creditcards").withEntity(httpEntity(s"""{}""")) ~> Route.seal(creditCardRoutes) ~> check {
        status shouldBe StatusCodes.BadRequest
        contentType shouldBe ContentTypes.`application/json`
        responseAs[Seq[ValidationError]] should contain theSameElementsAs
          Seq(ValidationError("invalid.input", "", "all required fields number provided with correct format"))
      }
    }

    "not be able to request credit card with incomplete body" in {
      Post("/creditcards").withEntity(httpEntity(s"""{"firstname": "saurabh", "lastname": "arora", "dob": "1983/03/12"}""")) ~> Route.seal(creditCardRoutes) ~> check {
        status shouldBe StatusCodes.BadRequest
        contentType shouldBe ContentTypes.`application/json`
        responseAs[Seq[ValidationError]] should contain theSameElementsAs
          Seq(ValidationError("invalid.input", "", "all required fields number provided with correct format"))
      }
    }

    "not be able to request credit card with invalid applicant data" in {
      val applicantJson =
        s"""{
           |"firstname": "",
           |"lastname": "",
           |"dob": "198/03/12",
           |"credit-score": 701,
           |"employment-status": "INVALID_STATUS",
           |"salary": -1
           |}""".stripMargin

      Post("/creditcards").withEntity(httpEntity(applicantJson)) ~> Route.seal(creditCardRoutes) ~> check {
        status shouldBe StatusCodes.BadRequest
        contentType shouldBe ContentTypes.`application/json`
        responseAs[Seq[ValidationError]] should contain theSameElementsAs
          Seq(
            ValidationError("invalid.value", "firstname", "firstname must be provided"),
            ValidationError("invalid.value", "lastname", "lastname must be provided"),
            ValidationError("invalid.value", "dob", "dob must be a string formatted as yyyy/MM/dd"),
            ValidationError("invalid.value", "credit-score", "credit-score must be a number between 0 and 700"),
            ValidationError("invalid.value", "employment-status", s"employment-status must be one of ${EmploymentStatus.values}"),
            ValidationError("invalid.value", "salary", "salary must be greater than equal to 0")
          )
      }
    }

    "be able to receive credit card offers when they provide valid information" in {

      import uk.co.agilesoftware.WiremockStub._

      given(post(urlPathEqualTo("/v1/cards")))
        .returns(Seq(
          csCardsResponse("SuperSaver Card", "http://www.example.com/apply", 21.4, 6.3),
          csCardsResponse("SuperSpender Card", "http://www.example.com/apply", 19.2, 5.0, "Interest free purchases for 6 months")
        ))

      given(post(urlPathEqualTo("/v2/creditcards")))
        .returns(Seq(scoredCardsResponse("ScoredCard Builder", "http://www.example.com/apply", 19.4, 0.8, Seq("Supports ApplePay"),
          Seq("Interest free purchases for 1 month"))))


      val applicantJson = s"""{"firstname": "saurabh","lastname": "arora","dob": "1980/07/03","credit-score": 500,
           |"employment-status": "FULL_TIME","salary": 10000}""".stripMargin

      import spray.json._

      implicit def wrapJsValue(jsValue: JsValue) = JsonVerification(jsValue)

      Post("/creditcards").withEntity(httpEntity(applicantJson)) ~> Route.seal(creditCardRoutes) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        responseAs[String].parseJson match {
          case JsArray(Vector(cardOne, cardTwo, cardThree)) =>
            cardOne contains ("ScoredCards", "ScoredCard Builder", "http://www.example.com/apply", 19.4, 0.212, "Supports ApplePay", "Interest free purchases for 1 month")
            cardTwo contains ("CSCards", "SuperSaver Card", "http://www.example.com/apply", 21.4, 0.137)
            cardThree contains ("CSCards", "SuperSpender Card", "http://www.example.com/apply", 19.2, 0.135, "Interest free purchases for 6 months")
          case json => fail(s"response $json doesn't match expectation")
        }
      }
    }

    "receive an empty list if CS Cards and Scored Cards are unreachable" in {
      val applicantJson = s"""{"firstname": "saurabh","lastname": "arora","dob": "1980/07/03","credit-score": 500,
                             |"employment-status": "FULL_TIME","salary": 10000}""".stripMargin

      Post("/creditcards").withEntity(httpEntity(applicantJson)) ~> Route.seal(creditCardRoutes) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        responseAs[String] shouldBe """[]"""
      }
    }
  }

  private case class JsonVerification(jsValue: JsValue) {
    def contains(card: String, name: String, url: String, apr: Double, cardscore: Double, features: String*): Unit = {
      jsValue.asJsObject shouldBe
        JsObject(
          "provider" -> JsString(card),
          "name" -> JsString(name),
          "apply-url" -> JsString(url),
          "apr" -> JsNumber(apr),
          "card-score" -> JsNumber(cardscore),
          "features" -> JsArray(features.map(JsString(_)).toVector)
        )
    }
  }
}
