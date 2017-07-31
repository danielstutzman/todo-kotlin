package integration_test

import db.Db
import org.cyberneko.html.parsers.SAXParser
import org.xml.sax.InputSource
import webapp.PostgresCredentials
import java.io.File
import java.io.FileWriter
import java.io.StringReader
import java.sql.DriverManager

const val EMAIL1 = "a@a.com"
const val SAVE_PATH = "src/test/resources/scraped"

fun main(args: Array<String>) {
  System.setProperty("org.jooq.no-logo", "true")

  val creds = PostgresCredentials(
      "localhost",
      5432,
      "disabled",
      "dan",
      "",
      "todo_rails_development"
  )
  val jdbcUrl = "jdbc:postgresql://${creds.hostname}:${creds.port}/${creds.databaseName}"
  val conn = DriverManager.getConnection(jdbcUrl, creds.username, creds.password)
  val db = Db(conn)

  runScenarios("http://localhost:3000", db) { scenarioName, htmlBody ->
    if (!File(SAVE_PATH).exists() && !File(SAVE_PATH).mkdirs()) {
      throw RuntimeException("Couldn't mkdirs ${SAVE_PATH}")
    }
    val outFile = File("${SAVE_PATH}/${scenarioName}.html")
    FileWriter(outFile).use { fileWriter ->
      val parser = SAXParser()
      parser.contentHandler = SAXWriteTagPerLine(fileWriter)
      parser.parse(InputSource(StringReader(htmlBody)))
    }
    println(outFile)
  }
}

