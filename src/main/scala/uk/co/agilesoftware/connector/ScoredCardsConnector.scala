package uk.co.agilesoftware.connector

import uk.co.agilesoftware.domain.{ Applicant, Card }

trait ScoredCardsConnector extends CardsConnector {

  implicit override val cardReader = Card.scoredCardsFormat

  override def requestBody(applicant: Applicant): String =
    s"""{"first-name": "${applicant.firstname}",
       |"last-name": "${applicant.lastname}",
       |"date-of-birth": "${applicant.dob}",
       |"score": ${applicant.creditScore},
       |"employment-status": "${applicant.employmentStatus}",
       |"salary": ${applicant.salary}
       |}""".stripMargin

}

object ScoredCardsConnector extends ScoredCardsConnector {
  override val url: String = "http://m33dnjs979.execute-api.us-west-2.amazonaws.com/CS/v2/creditcards"
}
