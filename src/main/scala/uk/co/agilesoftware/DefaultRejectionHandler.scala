package uk.co.agilesoftware

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{ MalformedRequestContentRejection, RejectionHandler }
import spray.json.DefaultJsonProtocol._

trait DefaultRejectionHandler extends SprayJsonSupport {

  implicit val validationErrorFormat = jsonFormat3(ValidationError)

  implicit def rejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handle {
        case MalformedRequestContentRejection(_, ex: ValidationException) => complete(StatusCodes.BadRequest, ex.errors)
      }
      .result()
}
