package uk.co.agilesoftware.domain

import org.scalatest.{ FlatSpec, Matchers }

class CardSpec extends FlatSpec with Matchers {

  "card-score" should "be computed as 10 * (eligibility * sq(1/apr))" in {
    Card("", "", "", 21.4, 6.3).cardScore shouldBe 0.137
    Card("", "", "", 19.4, 8.0).cardScore shouldBe 0.212
    Card("", "", "", 19.2, 5.0).cardScore shouldBe 0.135
  }

  "cards" should "be ordered by descending value of cardScore" in {
    val cardOne = Card("", "", "", 21.4, 6.3)
    val cardTwo = Card("", "", "", 19.4, 8.0)
    val cardThree = Card("", "", "", 19.2, 5.0)
    Seq(cardOne, cardTwo, cardThree).sorted should contain inOrderElementsOf Seq(cardTwo, cardOne, cardThree)
  }
}
