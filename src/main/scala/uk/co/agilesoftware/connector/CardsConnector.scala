package uk.co.agilesoftware.connector

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import uk.co.agilesoftware.domain.{Applicant, Card, InvalidResponseError}
import spray.json._

import scala.concurrent.{ExecutionContextExecutor, Future}

trait CardsConnector {
  private implicit def system: ActorSystem = ActorSystem()
  private implicit def materializer: ActorMaterializer = ActorMaterializer()
  private implicit lazy val executionContext: ExecutionContextExecutor = system.dispatcher

  private implicit val cardsReader: RootJsonReader[Seq[Card]] = {
    case JsArray(jsValues) => jsValues.map(cardReader.read(_))
    case json => throw new RuntimeException(s"Unable to read Cards response: $json")
  }

  implicit protected def cardReader: JsonReader[Card]
  protected def url: String
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
        case _ =>
          //Log dependency failure and mitigation
          Future.successful(Seq.empty[Card])
      }
    }.recover {
      case _: InvalidResponseError =>
        //Log parsing error and mitigation
        Seq.empty[Card]
    }
  }
}

