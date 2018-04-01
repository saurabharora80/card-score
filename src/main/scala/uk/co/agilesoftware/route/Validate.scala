package uk.co.agilesoftware.route

import scala.util.{ Failure, Success, Try }

case class ValidationException(errors: Seq[ValidationError]) extends RuntimeException

case class Constraint[T](apply: T => Boolean, error: ValidationError)
case class ValidationError(code: String, path: String, reason: String)

object InvalidValueError {
  def apply(fieldName: String, errorMessage: String) = ValidationError("invalid.value", fieldName, errorMessage)
}

object Validate {
  def apply[T](constraints: Constraint[T]*)(entity: T): Unit = {
    val errors = constraints.foldLeft(List.empty[ValidationError]) { (errors, constraint) =>
      Try(constraint.apply(entity)) match {
        case Success(result) => if (!result) constraint.error :: errors else errors
        case Failure(_) => constraint.error :: errors
      }
    }
    if (errors.nonEmpty) throw ValidationException(errors)
  }
}