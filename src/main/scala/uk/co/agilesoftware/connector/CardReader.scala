package uk.co.agilesoftware.connector

import spray.json.{ JsArray, JsNumber, JsString, JsValue, RootJsonReader }
import uk.co.agilesoftware.domain.Card

case class InvalidResponseError(cardType: String, json: JsValue) extends RuntimeException(s"Unable to parse json for $cardType: ${json.prettyPrint}")

object CardReader {
  import spray.json.DefaultJsonProtocol._

  private[connector] val csCardReader = new RootJsonReader[Card] {
    override def read(json: JsValue): Card = json.asJsObject.getFields("cardName", "url", "apr", "eligibility", "features") match {
      case Seq(JsString(cardName), JsString(url), JsNumber(apr), JsNumber(eligibility)) =>
        Card("CSCards", cardName, url, apr, eligibility)
      case Seq(JsString(cardName), JsString(url), JsNumber(apr), JsNumber(eligibility), JsArray(features)) =>
        Card("CSCards", cardName, url, apr, eligibility, features.map(_.convertTo[String]))
      case _ => throw new InvalidResponseError("CSCards", json)
    }
  }

  private[connector] val scoredCardReader = new RootJsonReader[Card] {
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
}
