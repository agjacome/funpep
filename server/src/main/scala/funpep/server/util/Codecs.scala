package funpep.server
package util

import java.util.UUID
import java.time.Instant

import argonaut._
import argonaut.Argonaut._

import atto._
import atto.syntax.parser._

import funpep.data._
import funpep.util.parsers._


private[util] trait Codecs {

  import Analysis._

  implicit def UUIDEncode: EncodeJson[UUID] =
    StringEncodeJson.contramap(_.toString)

  implicit def UUIDDecode: DecodeJson[UUID] =
    optionDecoder(_.string.flatMap(uuid.parseOnly(_).option), "UUID")

  implicit def InstantEncode: EncodeJson[Instant] =
    LongEncodeJson.contramap(_.getEpochSecond)

  implicit def InstantDecode: DecodeJson[Instant] =
    optionDecoder(_.number.flatMap(_.toLong).map(Instant.ofEpochSecond), "Instant")

  implicit def StatusEncode: EncodeJson[Status] =
    EncodeJson(_ match {
      case Created(t)   ⇒ ("status" := "created" ) ->: ("time" := t.getEpochSecond) ->: jEmptyObject
      case Started(t)   ⇒ ("status" := "started" ) ->: ("time" := t.getEpochSecond) ->: jEmptyObject
      case Finished(t)  ⇒ ("status" := "finished") ->: ("time" := t.getEpochSecond) ->: jEmptyObject
      case Failed(t, e) ⇒ ("status" := "failed"  ) ->: ("time" := t.getEpochSecond) ->: ("error" := e) ->: jEmptyObject
    })

  implicit def AnalysisEncode: EncodeJson[Analysis] =
    EncodeJson { a ⇒
      ("uuid"        := a.id)          ->:
      ("status"      := a.status)      ->:
      ("threshold"   := a.threshold)   ->:
      ("annotations" := a.annotations) ->:
      jEmptyObject
    }

  implicit def FastaEncode[A]: EncodeJson[Fasta[A]] =
    StringEncodeJson.contramap(fasta ⇒ FastaPrinter.toString(fasta))

  implicit def FastaDecode[A](implicit parser: Parser[A], ev: A ⇒ Compound): DecodeJson[Fasta[A]] =
    optionDecoder(_.string.flatMap(FastaParser.fromString(_).toOption), "Fasta")

}
