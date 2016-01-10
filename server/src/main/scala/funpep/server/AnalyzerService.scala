package funpep.server

import java.time.Instant.now

import scala.concurrent.ExecutionContext

import scalaz.concurrent._
import scalaz.stream._
import scalaz.std.option._
import scalaz.syntax.std.boolean._

import argonaut._
import argonaut.Argonaut._

import org.http4s._
import org.http4s.argonaut._
import org.http4s.dsl._
import org.http4s.server._

import funpep._
import funpep.data._
import funpep.util.functions._
import funpep.util.ops.path._

import util.codecs._
import util.extractors._
import util.functions._


final class AnalyzerService[A] private (val queue: AnalyzerQueue[A]) {

  def analyzer: Analyzer[A]    = queue.analyzer
  def parser:   FastaParser[A] = analyzer.parser

  def service(implicit ec: ExecutionContext): HttpService = HttpService {
    case GET -> Root / "queue"              ⇒ queueSize
    case GET -> Root / "queue" / UUID(uuid) ⇒ queuePosition(uuid)

    case GET -> Root / UUID(uuid)        ⇒ analysisData(uuid)
    case GET -> Root / UUID(uuid) / file ⇒ analysisFile(uuid, file)

    case POST -> Root ⇒ ???
  }

  def queueSize: Process[Task, Response] = {
    def content(size: Int): Json =
      ("size" := size) ->: ("time" := now) ->: jEmptyObject

    ok { content(queue.count) }
  }

  def queuePosition(uuid: java.util.UUID): Process[Task, Response] = {
    def content(pos: Int): Json =
      ("uuid" := uuid) ->: ("position" := pos) ->: ("time" := now) ->: jEmptyObject

    queue.position(uuid).cata(pos ⇒ ok(content(pos)), notFound)
  }

  def analysisData(uuid: java.util.UUID): Process[Task, Response] = {
    lazy val directory: java.nio.file.Path =
      analyzer.database / uuid.toString

    lazy val readAnalysis: Process[Task, Json] =
      AnalysisParser.fromFileW(directory / "analysis.data").map(_.asJson)

    directory.exists.flatMap(_ ? readAnalysis.flatMap(ok(_)) | notFound)
  }

  def analysisFile(uuid: java.util.UUID, file: String): Process[Task, Response] = {
    lazy val directory: java.nio.file.Path =
      analyzer.database / uuid.toString

    lazy val readAnalysis: Process[Task, Analysis] =
      AnalysisParser.fromFileW(directory / "analysis.data")

    directory.exists.flatMap(_ ? readAnalysis.flatMap( a ⇒ analysisFile(a, file)) | notFound)
  }

  private def analysisFile(analysis: Analysis, file: String): Process[Task, Response] =
    StaticFile.fromFile((analysis.directory / file).toFile, none[Request])
              .fold(notFound)(AsyncP(_))

}

object AnalyzerService {

  def apply[A](queue: AnalyzerQueue[A])(implicit ec: ExecutionContext): HttpService =
    new AnalyzerService(queue).service

}
