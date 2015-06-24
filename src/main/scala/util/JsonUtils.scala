package es.uvigo.ei.sing.funpep
package util

import java.nio.file.Path
import java.util.UUID

import scalaz._
import scalaz.Scalaz._

import argonaut._
import argonaut.Argonaut._


object JsonUtils {

  import \/.{ fromTryCatchThrowable ⇒ tryCatch }

  implicit val PathDecodeJson: DecodeJson[Path] =
    optionDecoder(_.string map (_.toPath.toAbsolutePath), "Path")

  implicit val PathEncodeJson: EncodeJson[Path] =
    StringEncodeJson.contramap(_.toAbsolutePath.toString)

  implicit val UUIDDecodeJson: DecodeJson[UUID] =
    optionDecoder(_.string >>= {
      str ⇒ tryCatch[UUID, IllegalArgumentException](UUID.fromString(str)).toOption
    }, "UUID")

  implicit val UUIDEncodeJson: EncodeJson[UUID] =
    StringEncodeJson.contramap(_.toString)

  def jsonErr(message: String): Json = Json("err":= message)
  def jsonErr(err: Throwable):  Json = jsonErr(err.getMessage)

}
