package integration_test

import org.cyberneko.html.parsers.SAXParser
import org.xml.sax.InputSource
import webapp.Config
import webapp.PostgresCredentials
import webapp.startServer
import java.io.File
import java.io.FileWriter
import java.io.StringReader
import java.sql.DriverManager

fun main(args: Array<String>) {
  System.setProperty("org.jooq.no-logo", "true")
  val creds = PostgresCredentials(
      "localhost",
      5432,
      "disable",
      "dan",
      "",
      "todo-kotlin")

  val jdbcUrl = "jdbc:postgresql://${creds.hostname}:${creds.port}/${creds.databaseName}"
  val conn = DriverManager.getConnection(jdbcUrl, creds.username, creds.password)
  val db = db.Db(conn)

  val service = startServer(Config(3001, creds))
  service.awaitInitialization()

  runScenarios("http://localhost:3001", db, { scenarioName, htmlBody ->
    val parser = SAXParser()

    val newFile = File("${scenarioName}.html")
    FileWriter(newFile).use { fileWriter ->
      parser.contentHandler = SAXWriteTagPerLine(fileWriter)
      parser.parse(InputSource(StringReader(htmlBody)))
    }

    val commandArgs = listOf(
        "/usr/bin/diff",
        "-u",
        "src/test/resources/scraped/${scenarioName}.html",
        newFile.path)
    ProcessBuilder(commandArgs)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor()

    newFile.delete()
    println(scenarioName)
  })

  service.stop()
}
