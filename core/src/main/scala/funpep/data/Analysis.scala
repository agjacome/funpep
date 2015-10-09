package funpep
package data

import java.nio.file.Path
import java.time.Instant
import java.util.{ StringJoiner, UUID }

import scalaz._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.std.string._
import scalaz.syntax.show._
import scalaz.syntax.applicative._

import atto._
import atto.parser.all._
import atto.syntax.parser._

import util.functions._
import util.types._
import util.parsers._
import util.ops.disjunction._
import util.ops.path._
import util.ops.string._


final case class Analysis (
  id:          UUID,
  status:      Analysis.Status,
  directory:   Path,
  threshold:   Double,
  annotations: Sequence.Header ==>> String
) {

  def withStatus(s: Analysis.Status): Analysis =
    this.copy(status = s)

  override def toString: String = {
    lazy val annsStr: String = (annotations.fold(new StringJoiner("\n")) {
      (k, v, sj) ⇒ sj.add(s""""$k" -> "$v"""")
    }).toString

    s"""|id: $id
        |status: ${status.shows}
        |
        |directory: "${directory.absolute}"
        |threshold: $threshold
        |
        |annotations:
        |$annsStr
        |""".stripMargin
  }

}

object Analysis {

  sealed trait Status { def timestamp: Instant }
  final case class Created  (timestamp: Instant) extends Status
  final case class Started  (timestamp: Instant) extends Status
  final case class Finished (timestamp: Instant) extends Status
  final case class Failed   (timestamp: Instant, err: ErrorMsg) extends Status

  object Status {

    implicit val StatusEqual: Equal[Status] = Equal.equalA

    implicit val StatusShow: Show[Status] = Show.shows({
      case Created(t)   ⇒ s"created:${t.getEpochSecond}"
      case Started(t)   ⇒ s"started:${t.getEpochSecond}"
      case Finished(t)  ⇒ s"finished:${t.getEpochSecond}"
      case Failed(t, e) ⇒ s"failed:${t.getEpochSecond}:$e"
    })

    lazy val parser: Parser[Status] = {
      lazy val instant: Parser[Instant] = long.map(Instant.ofEpochSecond)

      (string("created")  ~> sep(':') ~> instant).map(Created.apply)  |
      (string("started")  ~> sep(':') ~> instant).map(Started.apply)  |
      (string("finished") ~> sep(':') ~> instant).map(Finished.apply) |
      (string("failed")   ~> sep(':') ~> instant ~ (sep(':') ~> takeLine)).map(Failed.tupled)
    }

  }

  implicit val AnalysisEqual: Equal[Analysis] = Equal.equalA.contramap(_.id)
  implicit val AnalysisShow:  Show[Analysis]  = Show.showA

  def apply(path: Path, thres: Double, annots: Sequence.Header ==>> String): Analysis =
    new Analysis(UUID.randomUUID, Created(Instant.now), path, thres, annots)

}

object AnalysisParser {

  import Analysis.Status
  import Sequence.Header

  lazy val analysis: Parser[Analysis] = (
    (id          <~ skipWhitespace) |@|
    (status      <~ skipWhitespace) |@|
    (directory   <~ skipWhitespace) |@|
    (threshold   <~ skipWhitespace) |@|
    (annotations <~ skipWhitespace)
  ) { Analysis.apply }

  lazy val id:        Parser[UUID]   = string("id")        ~> sep(':') ~> uuid          <~ eol
  lazy val status:    Parser[Status] = string("status")    ~> sep(':') ~> Status.parser <~ eol
  lazy val directory: Parser[Path]   = string("directory") ~> sep(':') ~> stringLiteral <~ eol map (_.toPath)
  lazy val threshold: Parser[Double] = string("threshold") ~> sep(':') ~> double        <~ eol

  lazy val annotations: Parser[Header ==>> String] =
    annotHeader ~> sepBy(annot, skipWhitespaceLine) map { as ⇒ ==>>.fromList(as) }

  lazy val annotHeader: Parser[Unit] =
    string("annotations") ~> sep(':') ~> eol

  lazy val annot: Parser[(Header, String)] =
    stringLiteral ~ (annotationSep ~> stringLiteral)

  lazy val annotationSep: Parser[Unit] =
    (skipHorizontalWhitespace ~ string("->") ~ skipHorizontalWhitespace).void

  def fromString(str: String): ErrorMsg ∨ Analysis =
    analysis.parseOnly(str).either

  def fromFile(path: Path): Process[Task, ErrorMsg ∨ Analysis] =
    textR(path).map(fromString)

  def fromFileW(path: Path): Process[Task, Analysis] =
    fromFile(path) flatMap {
      parsed ⇒ Process.eval(parsed.toTask(identity))
    }

}

object AnalysisPrinter {

  def toString(analysis: Analysis): String =
    analysis.shows

  def toFile(analysis: Analysis, path: Path): Process[Task, Unit] =
    Process(analysis.shows).pipe(text.utf8Encode).to(nio.file.chunkW(path))

  def toStdOut(analysis: Analysis): Process[Task, Unit] =
    Process(analysis.shows).to(io.stdOut)

}
