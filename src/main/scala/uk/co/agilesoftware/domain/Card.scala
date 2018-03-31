package uk.co.agilesoftware.domain

case class Card(provider: String, cardName: String, url: String, apr: BigDecimal, eligibility: BigDecimal, features: Seq[String] = Seq.empty)

