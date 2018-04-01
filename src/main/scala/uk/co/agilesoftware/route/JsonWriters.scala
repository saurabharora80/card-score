package uk.co.agilesoftware.route

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ JsArray, JsNumber, JsObject, JsString, JsValue, RootJsonReader, RootJsonWriter }
import uk.co.agilesoftware.domain.{ Applicant, Card, EmploymentStatus }

object JsonWriters extends SprayJsonSupport {

  private[route] implicit val cardWriter: RootJsonWriter[Card] = card => JsObject(
    "provider" -> JsString(card.provider),
    "name" -> JsString(card.cardName),
    "apply-url" -> JsString(card.url),
    "apr" -> JsNumber(card.apr),
    "features" -> JsArray(card.features.map(JsString(_)).toVector),
    "card-score" -> JsNumber(card.cardScore)
  )

  private[route] implicit val cardsWriter: RootJsonWriter[Seq[Card]] = cards => JsArray(cards.map(cardWriter.write).toVector)

  private[route] implicit val applicantReader: RootJsonReader[Applicant] = new RootJsonReader[Applicant] {
    type ApplicantJson = (String, String, String, BigDecimal, String, BigDecimal)
    override def read(json: JsValue): Applicant = json.asJsObject.getFields("firstname", "lastname", "dob", "credit-score", "employment-status", "salary") match {
      case Seq(JsString(firstname), JsString(lastname), JsString(dob), JsNumber(creditScore), JsString(employmentStatus), JsNumber(salary)) =>
        Validate(
          Constraint[ApplicantJson](!_._1.isEmpty, InvalidValueError("firstname", "firstname must be provided")),
          Constraint[ApplicantJson](!_._2.isEmpty, InvalidValueError("lastname", "lastname must be provided")),
          Constraint[ApplicantJson](
            a => !a._3.isEmpty && LocalDate.parse(a._3, DateTimeFormatter.ofPattern("yyyy/MM/dd")).isBefore(LocalDate.now),
            InvalidValueError("dob", "dob must be a string formatted as yyyy/MM/dd")
          ),
          Constraint[ApplicantJson](
            a => a._4 >= 0 && a._4 <= 700,
            InvalidValueError("credit-score", "credit-score must be a number between 0 and 700")
          ),
          Constraint[ApplicantJson](
            a => EmploymentStatus.values.contains(EmploymentStatus.withName(a._5)),
            InvalidValueError("employment-status", s"employment-status must be one of ${EmploymentStatus.values}")
          ),
          Constraint[ApplicantJson](_._6 >= 0, InvalidValueError("salary", "salary must be greater than equal to 0"))
        )((firstname, lastname, dob, creditScore, employmentStatus, salary))

        Applicant(firstname, lastname, dob, creditScore.toInt, EmploymentStatus.withName(employmentStatus), salary.toInt)

      case _ => throw ValidationException(Seq(ValidationError("invalid.input", "", "all required fields number provided with correct format")))
    }
  }

}
