package uk.co.agilesoftware.domain

import uk.co.agilesoftware.domain.EmploymentStatus.EmploymentStatus

object EmploymentStatus extends Enumeration {
  type EmploymentStatus = Value
  val FULL_TIME, PART_TIME, STUDENT, UNEMPLOYED, RETIRED = Value
}

case class Applicant(firstname: String, lastname: String, dob: String, creditScore: Int, employmentStatus: EmploymentStatus, salary: Int) {
  def fullName = s"$firstname $lastname"
}
