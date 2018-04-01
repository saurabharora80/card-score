package uk.co.agilesoftware.connector

import uk.co.agilesoftware.Config
import uk.co.agilesoftware.domain.Applicant

trait ScoredCardsConnector extends CardsConnector {

  implicit override protected val cardReader = CardReader.scoredCardReader

  override private[connector] def requestBody(applicant: Applicant): String =
    s"""{"first-name": "${applicant.firstname}",
       |"last-name": "${applicant.lastname}",
       |"date-of-birth": "${applicant.dob}",
       |"score": ${applicant.creditScore},
       |"employment-status": "${applicant.employmentStatus}",
       |"salary": ${applicant.salary}
       |}""".stripMargin

}

object ScoredCardsConnector extends ScoredCardsConnector {
  override val url: String = Config.scoreCardsUrl
}
