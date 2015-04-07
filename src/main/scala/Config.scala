package es.uvigo.ei.sing.funpep

import java.nio.file.Path

import scalaz._
import scalaz.Scalaz._
import scalaz.effect.IO

import argonaut._
import argonaut.Argonaut._


final case class Config (clustalo: Path, nullPath: Path)

object Config {

  type Configured[A] = Reader[Config, A]

  def withConfig[A](f: Config ⇒ A): Configured[A] = Reader { f }

}

object ConfigParser {

  // TODO: move to top-level, call fromFile there
  val file: Path = path"${ System.getProperty("config.file", resource("config.json").getPath) }"

  def fromString(str: String): Parsed[Config] =
    str.decodeEither[Config] leftMap { err ⇒ new RuntimeException(err) }

  def fromFile(file: Path): IO[Parsed[Config]] =
    file.contentsAsString.catchLeft map {
      _.map(_ map fromString err s"Could not read config contents from $file").join
    }

  implicit def PathDecodeJson: DecodeJson[Path] =
    optionDecoder(_.string.map(str ⇒ path"$str"), "Path")

  implicit def ConfigDecodeJson: DecodeJson[Config] =
    jdecode2L(Config.apply)("clustalo", "nullPath")

}
