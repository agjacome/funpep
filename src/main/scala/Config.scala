package es.uvigo.ei.sing.funpep

import java.nio.file.Path

import scalaz._
import scalaz.Scalaz._
import scalaz.effect.IO

import argonaut._
import argonaut.Argonaut._


final case class Config (
  clustalo:     Path,
  nullPath:     Path,
  databasePath: Path,
  temporalPath: Path
)

object Config {

  type ConfiguredT[F[_], A] = ReaderT[F, Config, A]
  type Configured[A]        = ConfiguredT[Id, A]

  object ConfiguredT {
    def apply[F[_], A](f: Config ⇒ F[A]): ConfiguredT[F, A] = Kleisli[F, Config, A](f)
  }

  object Configured {
    def apply[A](f: Config ⇒ A): Configured[A] = Kleisli[Id, Config, A](f)
  }

}

object ConfigParser {

  // TODO: move to top-level, call fromFile there
  val file: Path = System.getProperty("config.file", resource("config.json").getPath).toPath

  def fromString(str: String): Throwable \/ Config =
    str.decodeEither[Config] leftMap { err ⇒ new RuntimeException(err) }

  def fromFile(file: Path): ErrorOrIO[Config] =
    file.contentsAsString >>= { str ⇒ EitherT(fromString(str).point[IO]) }

  implicit def PathDecodeJson: DecodeJson[Path] =
    optionDecoder(_.string.map(_.toPath.toAbsolutePath), "Path")

  implicit def ConfigDecodeJson: DecodeJson[Config] =
    jdecode4L(Config.apply)("clustalo", "nullPath", "databasePath", "temporalPath")

}
