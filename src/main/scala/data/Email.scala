package es.uvigo.ei.sing.funpep
package data

import scalaz._
import scalaz.Scalaz._

import argonaut._
import argonaut.Argonaut._


final class Email private (val value: String)

object Email extends (String ⇒ Option[Email]) {

  lazy val EmailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  implicit val EmailInstances = new Equal[Email] with Show[Email] {
    override def equal(e1: Email, e2: Email): Boolean =
      e1.value ≟ e2.value

    override def show(e: Email): Cord =
      Cord(e.value)
  }

  implicit val EmailDecodeJson: DecodeJson[Email] =
    optionDecoder(_.string >>= (Email.apply), "Email")

  implicit val EmailEncodeJson: EncodeJson[Email] =
    StringEncodeJson.contramap(_.value)


  def apply(str: String): Option[Email] =
    EmailRegex.findFirstMatchIn(str.trim).map(m ⇒ new Email(m.matched))

  def unapply(email: Email): Option[String] =
    email.value.some

}
