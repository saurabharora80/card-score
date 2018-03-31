package uk.co.agilesoftware.connector

import spray.json.{JsArray, JsNumber, JsString, JsValue, RootJsonFormat}
import uk.co.agilesoftware.domain.{Applicant, Card}

trait CSCardsConnector extends CardsConnector {

  import spray.json.DefaultJsonProtocol._
  implicit override val cardReader = new RootJsonFormat[Card] {
    override def read(json: JsValue): Card = json.asJsObject.getFields("cardName", "url", "apr", "eligibility", "features") match {
      case Seq(JsString(cardName), JsString(url), JsNumber(apr), JsNumber(eligibility)) =>
        Card("CSCards", cardName, url, apr, eligibility)
      case Seq(JsString(cardName), JsString(url), JsNumber(apr), JsNumber(eligibility), JsArray(features)) =>
        Card("CSCards", cardName, url, apr, eligibility, features.map(_.convertTo[String]))
      case _ => throw new RuntimeException(s"Unable to read CSCards response: ${json.prettyPrint}")
    }

    override def write(obj: Card): JsValue = throw new RuntimeException("json is never written")
  }

  override def requestBody(applicant: Applicant): String =
    s"""{"fullName": "${applicant.fullName}", "dateOfBirth": "${applicant.dob}","creditScore": ${applicant.creditScore}}""".stripMargin
}

object CSCardsConnector extends CSCardsConnector {
  override val url: String = "http://y4xvbk1ki5.execute-api.us-west-2.amazonaws.com/CS/v1/cards"
}