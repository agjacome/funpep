package es.uvigo.ei.sing.funpep
package data

import java.nio.file.Path
import java.util.UUID

import scalaz._
import scalaz.Scalaz._
import scalaz.effect.IO

import argonaut._
import argonaut.Argonaut._

import util.IOUtils._
import util.JsonUtils._


final case class Analysis (
  uuid:        UUID,
  status:      Analysis.Status,
  email:       Email,
  threshold:   Double,
  annotations: Map[FastaEntry.ID, String]
) {

  // aliases of AnalysisPrinter.to*
  def toJsonString: String = AnalysisPrinter.toJsonString(this)
  def toJsonFile(file: Path): ⇄[Unit] = AnalysisPrinter.toJsonFile(file)(this)

}

object Analysis {

  sealed trait Status
  case object Created  extends Status
  case object Queued   extends Status
  case object Started  extends Status
  case object Finished extends Status
  case object Failed   extends Status

  object Status {

    def apply(str: String): Option[Status] =
      str.toLowerCase.trim match {
        case "created"  ⇒ Created.some
        case "queued"   ⇒ Queued.some
        case "started"  ⇒ Started.some
        case "finished" ⇒ Finished.some
        case "failed"   ⇒ Failed.some
        case _          ⇒ none
      }

    implicit val StatusEqual: Equal[Status] = Equal.equalA
    implicit val StatusShow:  Show[Status]  = Show.showA

    implicit val StatusDecodeJson: DecodeJson[Status] =
      optionDecoder(_.string >>= (Status.apply), "Status")

    implicit val StatusEncodeJson: EncodeJson[Status] =
      StringEncodeJson.contramap(_.shows)

  }

  implicit val AnalysisCodecJson: CodecJson[Analysis] =
    casecodec5(Analysis.apply, Analysis.unapply)("uuid", "status", "email", "threshold", "annotations")

  // aliases of AnalysisParser.from*
  def apply(str:  String): Throwable ∨ Analysis = AnalysisParser.fromJsonString(str)
  def apply(file: Path  ): ⇄[Analysis] = AnalysisParser.fromJsonFile(file)

}

object AnalysisParser {

  def fromJsonString(str: String): Throwable ∨ Analysis =
    str.decodeEither[Analysis] leftMap { err ⇒ new IllegalArgumentException(err) }

  def fromJsonFile(file: Path): ⇄[Analysis] =
    file.contentsAsString >>= { str ⇒ EitherT(fromJsonString(str).point[IO]) }

}

object AnalysisPrinter {

  import scalaz.\/.{ fromTryCatchThrowable ⇒ tryCatch }

  def toJsonString(s: ⇒ Analysis): String =
    implicitly[EncodeJson[Analysis]].encode(s).nospaces

  def toJsonFile(file: Path)(s: ⇒ Analysis): ⇄[Unit] =
    EitherT(file.openIOWriter.bracket(_.closeIO) {
      writer ⇒ tryCatch[Unit, Throwable](writer.write(toJsonString(s))).point[IO]
    })

}
