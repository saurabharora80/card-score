package uk.co.agilesoftware.connector

import uk.co.agilesoftware.domain.{ Applicant, Card }

trait CSCardsConnector extends CardsConnector {
  implicit override val cardReader = Card.csCardsFormat

  override def requestBody(applicant: Applicant): String =
    s"""{"fullName": "${applicant.fullName}", "dateOfBirth": "${applicant.dob}","creditScore": ${applicant.creditScore}}""".stripMargin
}

object CSCardsConnector extends CSCardsConnector {
  override val url: String = "http://y4xvbk1ki5.execute-api.us-west-2.amazonaws.com/CS/v1/cards"
}