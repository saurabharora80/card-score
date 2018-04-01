package uk.co.agilesoftware.connector

import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpEntity, _ }
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.TcpIdleTimeoutException
import spray.json._
import uk.co.agilesoftware.Singletons
import uk.co.agilesoftware.domain.{ Applicant, Card }

import scala.concurrent.{ ExecutionContext, Future }

trait CardsConnector {
  import Singletons._

  private val http = Http()

  private lazy val logger = Logging(system, classOf[CardsConnector])

  private implicit val cardsReader: RootJsonReader[Seq[Card]] = {
    case JsArray(jsValues) => jsValues.map(cardReader.read(_))
    case json => throw new RuntimeException(s"Unable to read Cards response: $json")
  }

  implicit protected def cardReader: JsonReader[Card]
  protected def url: String
  private[connector] def requestBody(applicant: Applicant): String

  def getCards(applicant: Applicant)(implicit ec: ExecutionContext): Future[Seq[Card]] = {

    lazy val request = HttpRequest(method = HttpMethods.POST, uri = url,
      entity = HttpEntity(ContentTypes.`application/json`, requestBody(applicant)))

    http.singleRequest(request).flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) if entity.contentType == ContentTypes.`application/json` =>
        Unmarshal(entity).to[String].map { _.parseJson.convertTo[Seq[Card]] }
      case HttpResponse(code, _, entity, _) =>
        logger.warning(s"${request.method.value}:${request.uri} >> Failed with status: $code and content type ${entity.contentType}. [Returning empty card list]")
        Future.successful(Seq.empty[Card])
    }.recover {
      case ex: InvalidResponseError =>
        logger.warning(s"${request.method.value}:${request.uri} >> Unable to parse json: ${ex.json.compactPrint}. [Returning empty card list]")
        Seq.empty[Card]
      case ex: TcpIdleTimeoutException =>
        logger.warning(s"${request.method.value}:${request.uri} >> ${ex.getMessage}. [Returning empty card list]")
        Seq.empty[Card]
    }
  }
}

