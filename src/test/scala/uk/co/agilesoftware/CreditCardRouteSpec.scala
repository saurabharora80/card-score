package uk.co.agilesoftware

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import spray.json.DefaultJsonProtocol
import uk.co.agilesoftware.domain.EmploymentStatus

class CreditCardRouteSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with CreditCardRoutes {
  val httpEntity: (String) => HttpEntity.Strict = (str: String) => HttpEntity(ContentTypes.`application/json`, str)

  import DefaultJsonProtocol._
  private implicit val errorFormat = jsonFormat3(ValidationError)

  "client" should {
    "not be able to request credit card with empty body" in {
      Post("/creditcards").withEntity(httpEntity(s"""{}""")) ~> Route.seal(creditCardRoutes) ~> check {
        status shouldBe StatusCodes.BadRequest
        responseAs[Seq[ValidationError]] should contain theSameElementsAs
          Seq(ValidationError("invalid.input", "", "all required fields number provided with correct format"))
      }
    }

    "not be able to request credit card with incomplete body" in {
      Post("/creditcards").withEntity(httpEntity(s"""{"firstname": "saurabh", "lastname": "arora", "dob": "1983/03/12"}""")) ~> Route.seal(creditCardRoutes) ~> check {
        status shouldBe StatusCodes.BadRequest
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
  }
}
