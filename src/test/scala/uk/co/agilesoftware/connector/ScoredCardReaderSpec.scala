package uk.co.agilesoftware.connector

import org.scalatest.{ Matchers, WordSpec }
import spray.json.{ JsArray, JsNumber, JsObject, JsString }
import uk.co.agilesoftware.domain.Card

class ScoredCardReaderSpec extends WordSpec with Matchers {

  "Scored Card reader" should {
    "be able to read a valid CS Card response" in {
      CardReader.scoredCardReader.read(JsObject(
        "card" -> JsString("SuperSaver Card"),
        "apply-url" -> JsString("http://www.example.com/apply"),
        "annual-percentage-rate" -> JsNumber(21.4),
        "approval-rating" -> JsNumber(0.6),
        "attributes" -> JsArray(JsString("Supports ApplePay")),
        "introductory-offers" -> JsArray(JsString("Interest free purchase for 6 months"))
      )) shouldBe Card("ScoredCards", "SuperSaver Card", "http://www.example.com/apply", 21.4, 6.0, Seq("Supports ApplePay", "Interest free purchase for 6 months"))
    }

    "be able to read response if attributes and introductory-offers are missing" in {
      CardReader.scoredCardReader.read(JsObject(
        "card" -> JsString("SuperSaver Card"),
        "apply-url" -> JsString("http://www.example.com/apply"),
        "annual-percentage-rate" -> JsNumber(21.4),
        "approval-rating" -> JsNumber(0.5)
      )) shouldBe Card("ScoredCards", "SuperSaver Card", "http://www.example.com/apply", 21.4, 5.0)
    }

    "fail if mandatory field is missing " in {
      assertThrows[InvalidResponseError] {
        CardReader.scoredCardReader.read(JsObject(
          "card" -> JsString("SuperSaver Card"),
          "apply-url" -> JsString("http://www.example.com/apply"),
          "approval-rating" -> JsNumber(6.3)
        ))
      }
    }

    "fail if response is ill formed" in {
      assertThrows[InvalidResponseError] {
        CardReader.scoredCardReader.read(JsObject(
          "card" -> JsString("SuperSaver Card"),
          "apply-url" -> JsString("http://www.example.com/apply"),
          "annual-percentage-rate" -> JsString("21.4"),
          "approval-rating" -> JsNumber(6.3)
        ))
      }
    }
  }
}
