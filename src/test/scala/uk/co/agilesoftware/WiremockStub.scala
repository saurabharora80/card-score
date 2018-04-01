package uk.co.agilesoftware

import akka.http.scaladsl.model.StatusCode
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import scala.concurrent.duration._

object WiremockStub {
  def scoredCardsResponse(cardname: String, url: String, apr: Double, eligibility: Double,
    attributes: Seq[String] = Seq.empty, offers: Seq[String] = Seq.empty): String =
    s"""{"card": "$cardname", "apply-url": "$url", "annual-percentage-rate": $apr, "approval-rating": $eligibility,
       |"attributes": [${attributes.map(f => s""" "$f" """).mkString(",")}],
       |"introductory-offers": [${offers.map(f => s""" "$f" """).mkString(",")}]}""".stripMargin

  def csCardsResponse(cardname: String, url: String, apr: Double, eligibility: Double, features: String*): String =
    s"""{"cardName": "$cardname", "url": "$url", "apr": $apr, "eligibility": $eligibility,
     |"features": [${features.map(f => s""" "$f" """).mkString(",")}]}""".stripMargin

  class BuilderWrapper(builder: MappingBuilder) {
    def failsWith(failureStatus: StatusCode): StubMapping =
      stubFor(builder.willReturn(aResponse().withStatus(failureStatus.intValue()).withHeader("Content-Type", "application/json")))

    def returns(cards: Seq[String])(implicit delay: Duration = 0.seconds): StubMapping =
      stubFor(builder.willReturn(aResponse().withStatus(200).withFixedDelay(delay.toMillis.toInt)
        .withHeader("Content-Type", "application/json")
        .withBody(s"[${cards.mkString(",")}]")))
  }

  def given(builder: MappingBuilder) = new BuilderWrapper(builder)

}
