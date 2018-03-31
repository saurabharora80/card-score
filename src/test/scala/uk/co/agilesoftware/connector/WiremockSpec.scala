package uk.co.agilesoftware.connector

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

trait WiremockSpec extends BeforeAndAfterAll with BeforeAndAfterEach {
  self: Suite =>

  private val wiremockHost = "localhost"
  private val wiremockPort = 11111

  protected val wiremockUrl = s"http://$wiremockHost:$wiremockPort"

  private lazy val wireMockServer = new WireMockServer(wiremockPort)

  WireMock.configureFor(wiremockHost, 11111)

  override protected def beforeAll(): Unit = wireMockServer.start()

  override protected def afterAll(): Unit = wireMockServer.stop()

  override def beforeEach(): Unit = WireMock.reset()

}