package uk.co.agilesoftware

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ JsArray, JsNumber, JsObject, JsString, RootJsonWriter }
import uk.co.agilesoftware.domain.Card

object JsonWriters extends SprayJsonSupport {

  implicit val cardWriter: RootJsonWriter[Card] = card => JsObject(
    "provider" -> JsString(card.provider),
    "name" -> JsString(card.cardName),
    "apply-url" -> JsString(card.url),
    "apr" -> JsNumber(card.apr),
    "features" -> JsArray(card.features.map(JsString(_)).toVector),
    "card-score" -> JsNumber(card.cardScore)
  )

  implicit val cardsWriter: RootJsonWriter[Seq[Card]] = cards => JsArray(cards.map(cardWriter.write).toVector)

}
