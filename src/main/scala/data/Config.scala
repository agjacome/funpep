package es.uvigo.ei.sing.funpep
package data

import java.nio.file.Path

import scalaz._
import scalaz.Scalaz._
import scalaz.effect.IO

import argonaut._
import argonaut.Argonaut._

import util.IOUtils._
import util.JsonUtils._


final case class Config (
  clustalo:     Path,
  nullPath:     Path,
  databasePath: Path,
  temporalPath: Path
)

object Config {

  implicit val ConfigDecodeJson: DecodeJson[Config] =
    jdecode4L(Config.apply)("clustalo", "nullPath", "databasePath", "temporalPath")

  // aliases  of ConfigParser.from*
  def apply(str:  String): Throwable ∨ Config = ConfigParser.fromJsonString(str)
  def apply(file: Path  ): ⇄[Config]          = ConfigParser.fromJsonFile(file)

  object syntax {

    type ConfiguredT[F[_], A] = ReaderT[F, Config, A]
    type Configured[A]        = ConfiguredT[Id, A]

    def ConfiguredT[F[_], A](f: Config ⇒ F[A]): ConfiguredT[F, A] =
      Kleisli[F, Config, A](f)

    def Configured[A](f: Config ⇒ A): Configured[A] =
      Kleisli[Id, Config, A](f)

  }

}

object ConfigParser {

  // TODO: move to top-level, call fromJsonFile there
  val file: Path = (property("config.file") | resource("config.json").getPath).toPath

  def fromJsonString(str: String): Throwable ∨ Config =
    str.decodeEither[Config] leftMap { err ⇒ new IllegalArgumentException(err) }

  def fromJsonFile(file: Path): ⇄[Config] =
    file.contentsAsString >>= { str ⇒ fromJsonString(str).point[IO] }

}
