package funpep.server
package service

import java.time.Instant.now

import scala.concurrent.ExecutionContext

import scalaz.concurrent._
import scalaz.stream._
import scalaz.std.option._
import scalaz.std.string._
import scalaz.syntax.std.boolean._

import atto._

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

import funpep.server.util.codecs._
import funpep.server.util.extractors._
import funpep.server.util.functions._


final class AnalyzerService[A] private (
  val queue: AnalyzerQueue[A]
)(implicit parser: Parser[A], ev: A ⇒ Compound) {

  import AnalyzerService._

  def analyzer: Analyzer[A] = queue.analyzer

  def service(implicit ec: ExecutionContext): HttpService = HttpService {
    case GET -> Root / "queue"              ⇒ queueSize
    case GET -> Root / "queue" / UUID(uuid) ⇒ queuePosition(uuid)

    case GET -> Root / UUID(uuid)        ⇒ analysisData(uuid)
    case GET -> Root / UUID(uuid) / file ⇒ analysisFile(uuid, file)
    case req @ POST -> Root ⇒ req.decode[AnalysisWrapper[A]](createAnalysis)
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

  def createAnalysis(aw: AnalysisWrapper[A]): Process[Task, Response] =
    queue.push(aw.reference, aw.comparing, aw.threshold, aw.annotations) flatMap {
      analysis ⇒ ok(analysis.asJson)
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

    directory.exists.flatMap(_ ? readAnalysis.flatMap(analysisFile(_, file)) | notFound)
  }

  // FIXME: performs effect and wraps result in Process, this is wrong
  private def analysisFile(analysis: Analysis, file: String): Process[Task, Response] = {
    val f = (analysis.directory / file).toFile
    StaticFile.fromFile(f, none[Request]).fold(notFound)(AsyncP(_))
  }

}

object AnalyzerService {

  final case class AnalysisWrapper[A](
    reference:   Fasta[A],
    comparing:   Fasta[A],
    threshold:   Double,
    annotations: Analysis.Annotations
  )

  object AnalysisWrapper {

    implicit def WrapperDecode[A](implicit p: Parser[A], ev: A ⇒ Compound): DecodeJson[AnalysisWrapper[A]] =
      jdecode4L(AnalysisWrapper.apply[A])("reference", "comparing", "threshold", "annotations")

    implicit def WrapperEntityDecoder[A](implicit p: Parser[A], ev: A ⇒ Compound): EntityDecoder[AnalysisWrapper[A]] =
      jsonOf[AnalysisWrapper[A]]

  }

  def apply[A](queue: AnalyzerQueue[A])(implicit p: Parser[A], ev: A ⇒ Compound): AnalyzerService[A] =
    new AnalyzerService(queue)

  def service[A](queue: AnalyzerQueue[A])(implicit p: Parser[A], ev: A ⇒ Compound, ec: ExecutionContext): HttpService =
    new AnalyzerService(queue).service

}
