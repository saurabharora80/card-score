package uk.co.agilesoftware.domain

case class CreditCard(provider: String, name: String, applyUrl: String, apr: Double, features: Seq[String], cardScore: Double)



