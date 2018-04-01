package uk.co.agilesoftware.domain

import spray.json.{ JsArray, JsNumber, JsObject, JsString, JsValue, RootJsonFormat }

import scala.math.BigDecimal.RoundingMode

case class Card(provider: String, cardName: String, url: String, apr: BigDecimal, eligibility: BigDecimal, features: Seq[String] = Seq.empty) {
  def cardScore: BigDecimal = (10 * (eligibility * Math.pow((1 / apr).toDouble, 2))).setScale(3, RoundingMode.DOWN)

}

object Card {

  implicit val ordering = new Ordering[Card] {
    override def compare(c1: Card, c2: Card): Int = c2.cardScore compare c1.cardScore
  }

  import spray.json.DefaultJsonProtocol._
  implicit val csCardsFormat = new RootJsonFormat[Card] {
    override def read(json: JsValue): Card = json.asJsObject.getFields("cardName", "url", "apr", "eligibility", "features") match {
      case Seq(JsString(cardName), JsString(url), JsNumber(apr), JsNumber(eligibility)) =>
        Card("CSCards", cardName, url, apr, eligibility)
      case Seq(JsString(cardName), JsString(url), JsNumber(apr), JsNumber(eligibility), JsArray(features)) =>
        Card("CSCards", cardName, url, apr, eligibility, features.map(_.convertTo[String]))
      case _ => throw new InvalidResponseError("CSCards", json)
    }

    override def write(obj: Card): JsValue = throw new RuntimeException("json is never written")
  }

  implicit val scoredCardsFormat = new RootJsonFormat[Card] {
    override def read(json: JsValue): Card = json.asJsObject.getFields("card", "apply-url", "annual-percentage-rate", "approval-rating", "attributes", "introductory-offers") match {
      case Seq(JsString(cardName), JsString(url), JsNumber(apr), JsNumber(eligibility), JsArray(attributes), JsArray(offers)) =>
        Card("ScoredCards", cardName, url, apr, eligibility * 10, attributes.map(_.convertTo[String]) ++ offers.map(_.convertTo[String]))
      case Seq(JsString(cardName), JsString(url), JsNumber(apr), JsNumber(eligibility), JsArray(attributes)) =>
        Card("ScoredCards", cardName, url, apr, eligibility * 10, attributes.map(_.convertTo[String]))
      case Seq(JsString(cardName), JsString(url), JsNumber(apr), JsNumber(eligibility)) =>
        Card("ScoredCards", cardName, url, apr, eligibility * 10)
      case _ => throw new InvalidResponseError("ScoredCards", json)
    }

    override def write(card: Card): JsValue =
      JsObject(
        "provider" -> JsString(card.provider),
        "name" -> JsString(card.cardName),
        "apply-url" -> JsString(card.url),
        "apr" -> JsNumber(card.apr),
        "features" -> JsArray(card.features.map(JsString(_)).toVector),
        "card-score" -> JsNumber(card.cardScore)
      )
  }

}

class InvalidResponseError(cardType: String, json: JsValue) extends RuntimeException(s"Unable to parse json for $cardType: ${json.prettyPrint}")

