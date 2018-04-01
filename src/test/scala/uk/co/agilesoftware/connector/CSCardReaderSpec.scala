package uk.co.agilesoftware.connector

import org.scalatest.{ Matchers, WordSpec }
import spray.json.{ JsArray, JsNumber, JsObject, JsString }
import uk.co.agilesoftware.domain.Card

class CSCardReaderSpec extends WordSpec with Matchers {

  "CS Card reader" should {
    "be able to read a valid CS Card response" in {
      CardReader.csCardReader.read(JsObject(
        "cardName" -> JsString("SuperSaver Card"),
        "url" -> JsString("http://www.example.com/apply"),
        "apr" -> JsNumber(21.4),
        "eligibility" -> JsNumber(6.3),
        "features" -> JsArray(JsString("Interest free purchase for 6 months"))
      )) shouldBe Card("CSCards", "SuperSaver Card", "http://www.example.com/apply", 21.4, 6.3, Seq("Interest free purchase for 6 months"))
    }

    "be able to read response if features is missing" in {
      CardReader.csCardReader.read(JsObject(
        "cardName" -> JsString("SuperSaver Card"),
        "url" -> JsString("http://www.example.com/apply"),
        "apr" -> JsNumber(21.4),
        "eligibility" -> JsNumber(6.3)
      )) shouldBe Card("CSCards", "SuperSaver Card", "http://www.example.com/apply", 21.4, 6.3)
    }

    "fail if mandatory field is missing " in {
      assertThrows[InvalidResponseError] {
        CardReader.csCardReader.read(JsObject(
          "cardName" -> JsString("SuperSaver Card"),
          "url" -> JsString("http://www.example.com/apply"),
          "eligibility" -> JsNumber(6.3)
        ))
      }
    }

    "fail if response is ill formed" in {
      assertThrows[InvalidResponseError] {
        CardReader.csCardReader.read(JsObject(
          "cardName" -> JsString("SuperSaver Card"),
          "url" -> JsString("http://www.example.com/apply"),
          "apr" -> JsString("21.4"),
          "eligibility" -> JsNumber(6.3)
        ))
      }
    }
  }
}
