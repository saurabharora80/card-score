package uk.co.agilesoftware.route

import org.scalatest.{ Matchers, WordSpec }
import spray.json.{ JsNumber, JsObject, JsString }
import uk.co.agilesoftware.domain.{ Applicant, EmploymentStatus }

import scala.util.{ Failure, Success, Try }

class ApplicantValidationSpec extends WordSpec with Matchers {

  private class ValidationErrorAssertions(applicant: => Applicant) {
    def shouldFailWith(errors: ValidationError*): Unit =
      Try(applicant) match {
        case Failure(ex: ValidationException) => ex.errors should contain theSameElementsAs errors

        case Success(_) => fail("should have failed with a Validation exception")
        case Failure(ex) => fail(s"should have failed with a Validation exception; instead got $ex", ex)
      }
  }

  private implicit def assertThat(applicant: => Applicant): ValidationErrorAssertions = new ValidationErrorAssertions(applicant)

  private implicit def tupleToJsObject(values: (String, String, String, Any, String, Any)): JsObject = {
    def stringOrNumber(value: Any) = value match {
      case number: Int => JsNumber(number)
      case string: String => JsString(string)
      case _ => throw new RuntimeException("credit-score should be either number or string")
    }

    JsObject(
      "firstname" -> JsString(values._1),
      "lastname" -> JsString(values._2),
      "dob" -> JsString(values._3),
      "credit-score" -> stringOrNumber(values._4),
      "employment-status" -> JsString(values._5),
      "salary" -> stringOrNumber(values._6)
    )
  }

  "applicant" should {
    "have a firstname" in {
      JsonWriters.applicantReader.read(("", "arora", "1983/03/07", 500, "FULL_TIME", 1000)) shouldFailWith
        ValidationError("invalid.value", "firstname", "firstname must be provided")
    }

    "have a lastname" in {
      JsonWriters.applicantReader.read(("saurabh", "", "1983/03/07", 500, "PART_TIME", 1000)) shouldFailWith
        ValidationError("invalid.value", "lastname", "lastname must be provided")
    }

    "have a dob" in {
      JsonWriters.applicantReader.read(("saurabh", "arora", "", 500, "STUDENT", 1000)) shouldFailWith
        ValidationError("invalid.value", "dob", "dob must be a string formatted as yyyy/MM/dd")
    }

    "have a valid dob" in {
      Seq("199/03/07", "1980/32/11", "1980/22/13", "2030/12/11").foreach { invaliddob =>
        JsonWriters.applicantReader.read(("saurabh", "arora", invaliddob, 0, "UNEMPLOYED", 1000)) shouldFailWith
          ValidationError("invalid.value", "dob", "dob must be a string formatted as yyyy/MM/dd")
      }
    }

    "have a creditScore greater than equal to 0" in {
      JsonWriters.applicantReader.read(("saurabh", "arora", "1983/03/07", -1, "RETIRED", 1000)) shouldFailWith
        ValidationError("invalid.value", "credit-score", "credit-score must be a number between 0 and 700")
    }

    "have a creditScore less that equal to 700" in {
      JsonWriters.applicantReader.read(("saurabh", "arora", "1983/03/07", 701, "FULL_TIME", 1000)) shouldFailWith
        ValidationError("invalid.value", "credit-score", "credit-score must be a number between 0 and 700")
    }

    "have an valid employment status" in {
      JsonWriters.applicantReader.read(("saurabh", "arora", "1983/03/07", 700, "INVALID_STATUS", 1000)) shouldFailWith
        ValidationError("invalid.value", "employment-status", s"employment-status must be one of ${EmploymentStatus.values}")
    }

    "have a salary greater than equal to 0" in {
      JsonWriters.applicantReader.read(("saurabh", "arora", "1983/03/07", 700, "FULL_TIME", -1)) shouldFailWith
        ValidationError("invalid.value", "salary", "salary must be greater than equal to 0")
    }

    "not be created with invalid values" in {
      JsonWriters.applicantReader.read(("", "", "198/03/07", -1, "", -1)) shouldFailWith
        (
          ValidationError("invalid.value", "firstname", "firstname must be provided"),
          ValidationError("invalid.value", "lastname", "lastname must be provided"),
          ValidationError("invalid.value", "dob", "dob must be a string formatted as yyyy/MM/dd"),
          ValidationError("invalid.value", "credit-score", "credit-score must be a number between 0 and 700"),
          ValidationError("invalid.value", "employment-status", s"employment-status must be one of ${EmploymentStatus.values}"),
          ValidationError("invalid.value", "salary", "salary must be greater than equal to 0")
        )
    }

    "not be created if all values are not provided" in {
      JsonWriters.applicantReader.read(JsObject()) shouldFailWith
        ValidationError("invalid.input", "", "all required fields number provided with correct format")
    }

    "be created with all valid values" in {
      JsonWriters.applicantReader.read(("saurabh", "arora", "1983/03/07", 500, "FULL_TIME", 1000)) shouldBe
        Applicant("saurabh", "arora", "1983/03/07", 500, EmploymentStatus.FULL_TIME, 1000)

      JsonWriters.applicantReader.read(("saurabh", "arora", "1983/03/07", 0, "PART_TIME", 0)) shouldBe
        Applicant("saurabh", "arora", "1983/03/07", 0, EmploymentStatus.PART_TIME, 0)

      JsonWriters.applicantReader.read(("saurabh", "arora", "1983/03/07", 700, "STUDENT", 1000)) shouldBe
        Applicant("saurabh", "arora", "1983/03/07", 700, EmploymentStatus.STUDENT, 1000)
    }

  }
}

