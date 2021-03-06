package uk.co.agilesoftware.connector

import uk.co.agilesoftware.Config
import uk.co.agilesoftware.domain.Applicant

trait CSCardsConnector extends CardsConnector {
  implicit override protected val cardReader = CardReader.csCardReader

  override private[connector] def requestBody(applicant: Applicant): String =
    s"""{
       |"fullName": "${applicant.fullName}",
       |"dateOfBirth": "${applicant.dob}",
       |"creditScore": ${applicant.creditScore}
       }""".stripMargin
}

object CSCardsConnector extends CSCardsConnector {
  override val url: String = Config.csCardsUrl
}