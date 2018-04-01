package uk.co.agilesoftware.route

import org.scalatest.{ Matchers, WordSpec }

import scala.util.{ Failure, Try }

class ValidateSpec extends WordSpec with Matchers {

  "Validate" should {
    "fail with Validation exception when some of the validation conditions are met" in {
      Try(Validate(
        Constraint[String](_ => false, InvalidValueError("field1", "message-for-field1")),
        Constraint[String](_ => true, InvalidValueError("field2", "message-for-field2")),
        Constraint[String](_ => false, InvalidValueError("field3", "message-for-field3"))
      )("")) match {
        case Failure(ex: ValidationException) =>
          ex.errors should contain allElementsOf Seq(InvalidValueError("field1", "message-for-field1"), InvalidValueError("field3", "message-for-field3"))
        case _ => fail("validations should have failed")
      }
    }
  }
}
