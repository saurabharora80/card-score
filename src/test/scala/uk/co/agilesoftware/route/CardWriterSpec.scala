package uk.co.agilesoftware.route

import org.scalatest.{ FlatSpec, Matchers }
import spray.json.{ JsArray, JsNumber, JsObject, JsString }
import uk.co.agilesoftware.domain.Card

class CardWriterSpec extends FlatSpec with Matchers {

  "card" should "be written correctly as json" in {
    JsonWriters.cardWriter.write(Card("ScoredCard", "SuperSaver Card", "http://www.example.com/apply", 21.4, 6.3, Seq("featureOne"))) shouldBe
      JsObject(
        "provider" -> JsString("ScoredCard"),
        "name" -> JsString("SuperSaver Card"),
        "apply-url" -> JsString("http://www.example.com/apply"),
        "apr" -> JsNumber(21.4),
        "features" -> JsArray(JsString("featureOne")),
        "card-score" -> JsNumber(0.137)
      )
  }

}
