package uk.co.agilesoftware.domain

import scala.math.BigDecimal.RoundingMode

case class Card(provider: String, cardName: String, url: String, apr: BigDecimal, eligibility: BigDecimal, features: Seq[String] = Seq.empty) {
  def cardScore: BigDecimal = (10 * (eligibility * Math.pow((1 / apr).toDouble, 2))).setScale(3, RoundingMode.DOWN)

}

object Card {
  implicit val ordering = new Ordering[Card] {
    override def compare(c1: Card, c2: Card): Int = c2.cardScore compare c1.cardScore
  }
}

