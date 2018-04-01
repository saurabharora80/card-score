package uk.co.agilesoftware.connector

import spray.json.{ JsArray, JsNumber, JsString, JsValue, RootJsonReader }
import uk.co.agilesoftware.domain.{ Applicant, Card, InvalidResponseError }

trait ScoredCardsConnector extends CardsConnector {

  import spray.json.DefaultJsonProtocol._

  implicit override val cardReader = new RootJsonReader[Card] {
    override def read(json: JsValue): Card = json.asJsObject.getFields("card", "apply-url", "annual-percentage-rate", "approval-rating", "attributes", "introductory-offers") match {
      case Seq(JsString(cardName), JsString(url), JsNumber(apr), JsNumber(eligibility), JsArray(attributes), JsArray(offers)) =>
        Card("ScoredCards", cardName, url, apr, eligibility * 10, attributes.map(_.convertTo[String]) ++ offers.map(_.convertTo[String]))
      case Seq(JsString(cardName), JsString(url), JsNumber(apr), JsNumber(eligibility), JsArray(attributes)) =>
        Card("ScoredCards", cardName, url, apr, eligibility * 10, attributes.map(_.convertTo[String]))
      case Seq(JsString(cardName), JsString(url), JsNumber(apr), JsNumber(eligibility)) =>
        Card("ScoredCards", cardName, url, apr, eligibility * 10)
      case _ => throw new InvalidResponseError("ScoredCards", json)
    }
  }

  override def requestBody(applicant: Applicant): String =
    s"""{"first-name": "${applicant.firstname}",
       |"last-name": "${applicant.lastname}",
       |"date-of-birth": "${applicant.dob}",
       |"score": ${applicant.creditScore},
       |"employment-status": "${applicant.employmentStatus}",
       |"salary": ${applicant.salary}
       |}""".stripMargin

}

object ScoredCardsConnector extends ScoredCardsConnector {
  override val url: String = "http://m33dnjs979.execute-api.us-west-2.amazonaws.com/CS/v2/creditcards"
}
