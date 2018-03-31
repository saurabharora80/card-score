package uk.co.agilesoftware.connector

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import uk.co.agilesoftware.domain.{Applicant, Card}
import spray.json._
import scala.concurrent.Future

trait CardsConnector {
  implicit protected def system: ActorSystem = ActorSystem()
  implicit protected def materializer: ActorMaterializer = ActorMaterializer()
  private implicit lazy val executionContext = system.dispatcher

  protected def url: String

  import spray.json.DefaultJsonProtocol._
  implicit protected def cardReader: RootJsonFormat[Card]

  protected def requestBody(applicant: Applicant): String

  def getCards(applicant: Applicant): Future[Seq[Card]] = {

    lazy val getCardsRequest = HttpRequest(method = HttpMethods.POST, uri = url,
      entity = HttpEntity(ContentTypes.`application/json`, requestBody(applicant)))

    Http().singleRequest(getCardsRequest).flatMap { response =>
      response.status match {
        case StatusCodes.OK if response.entity.contentType == ContentTypes.`application/json` =>
          Unmarshal(response.entity).to[String].map { responseJson =>
            responseJson.parseJson.convertTo[Seq[Card]]
          }
        case status => throw new RuntimeException(s"${getCardsRequest.method}:${getCardsRequest.uri} failed with Status: $status and Content-Type: ${response.entity.contentType}")
      }
    }
  }
}


