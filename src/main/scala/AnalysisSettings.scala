package es.uvigo.ei.sing.funpep

import java.nio.file.Path

import scalaz._
import scalaz.Scalaz._
import scalaz.effect.IO

import argonaut._
import argonaut.Argonaut._


final case class AnalysisSettings (
  uuid:        String,
  email:       Email,
  threshold:   Double,
  annotations: Map[FastaEntry.ID, String]
) {

  // alias of AnalysisSettingsPrinter.to*
  def toJsonString: String = AnalysisSettingsPrinter.toJsonString(this)
  def toJsonFile(file: Path): ErrorOrIO[Unit] = AnalysisSettingsPrinter.toJsonFile(file)(this)

}

object AnalysisSettings {

  implicit val SettincsCodecJson: CodecJson[AnalysisSettings] =
    casecodec4(AnalysisSettings.apply, AnalysisSettings.unapply)("uuid", "email", "threshold", "annotations")

  // aliases of AnalysisSettingsParser.from*
  def apply(str:  String): Throwable \/ AnalysisSettings = AnalysisSettingsParser.fromJsonString(str)
  def apply(file: Path  ): ErrorOrIO[AnalysisSettings]   = AnalysisSettingsParser.fromJsonFile(file)

}

object AnalysisSettingsParser {

  def fromJsonString(str: String): Throwable \/ AnalysisSettings =
    str.decodeEither[AnalysisSettings] leftMap { err ⇒ new IllegalArgumentException(err) }

  def fromJsonFile(file: Path): ErrorOrIO[AnalysisSettings] =
    file.contentsAsString >>= { str ⇒ EitherT(fromJsonString(str).point[IO]) }

}

object AnalysisSettingsPrinter {

  import scalaz.\/.{ fromTryCatchThrowable ⇒ tryCatch }

  def toJsonString(s: ⇒ AnalysisSettings): String =
    implicitly[EncodeJson[AnalysisSettings]].encode(s).nospaces

  def toJsonFile(file: Path)(s: ⇒ AnalysisSettings): ErrorOrIO[Unit] =
    EitherT(file.openIOWriter.bracket(_.closeIO) {
      writer ⇒ tryCatch[Unit, Throwable](writer.write(toJsonString(s))).point[IO]
    })

}
