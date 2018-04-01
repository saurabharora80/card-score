package uk.co.agilesoftware

import com.typesafe.config.ConfigFactory

object Config {
  private val config = ConfigFactory.load()

  lazy val host: String = config.getString("http.host")
  lazy val port: Int = System.getProperty("HTTP_PORT", config.getInt("http.port").toString).toInt
  lazy val scoreCardsUrl: String = System.getProperty("SCOREDCARDS_ENDPOINT", config.getString("connector.scoredcards.url"))
  lazy val csCardsUrl: String = System.getProperty("CSCARDS_ENDPOINT", config.getString("connector.cscards.url"))
}
