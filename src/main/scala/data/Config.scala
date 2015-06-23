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

// TODO: Replace JSON file with HOCON or Java Properties
final case class Config (
  httpHost:   String,
  httpPort:   Int,
  httpPath:   String,
  httpDigest: Boolean,
  httpGzip:   Boolean,
  clustalo:   String,
  database:   Path,
  temporal:   Path,
  jobQueue:   Path
)

object Config {

  implicit val ConfigDecodeJson: DecodeJson[Config] =
    jdecode9L(Config.apply)(
      "http_host",
      "http_port",
      "http_path",
      "http_digest",
      "http_gzip",
      "clustalo",
      "database",
      "temporal",
      "job_queue"
    )

  // aliases  of ConfigParser.from*
  def apply(str:  String): Throwable ∨ Config  = ConfigParser.fromJsonString(str)
  def apply(file: Path  ): IOThrowable[Config] = ConfigParser.fromJsonFile(file)

  def default: Config =
    Config(
      httpHost   = "localhost",
      httpPort   = 8080,
      httpPath   = "/funpep",
      httpDigest = true,
      httpGzip   = true,
      clustalo   = "clustalo",
      database   = "database".toPath.toAbsolutePath,
      temporal   = "database/temporal".toPath.toAbsolutePath,
      jobQueue   = "database/job_queue".toPath.toAbsolutePath
    )

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

  def fromJsonString(str: String): Throwable ∨ Config =
    str.decodeEither[Config] leftMap { err ⇒ new IllegalArgumentException(err) }

  def fromJsonFile(file: Path): IOThrowable[Config] =
    file.contentsAsString >>= { str ⇒ fromJsonString(str).point[IO] }

}
