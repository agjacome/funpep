package funpep
package util

import java.util.UUID

import scalaz.syntax.applicative._

import atto._
import atto.parser.all._
import atto.syntax.parser._


private[util] trait Parsers {

  def eoi: Parser[Unit] = atto.parser.combinator.endOfInput
  def eol: Parser[Unit] = (cr | lf | cr ~ lf) map { _ ⇒ () }
  def cr:  Parser[Char] = char(0x0D)
  def lf:  Parser[Char] = char(0x0A)

  def takeLine: Parser[String] =
    takeWhile(c ⇒ c != '\r' && c != '\n')

  def skipHorizontalWhitespace: Parser[Unit] =
    many(horizontalWhitespace).void

  def skipWhitespaceLine: Parser[Unit] =
    skipHorizontalWhitespace <~ eol

  def sep(s: Char): Parser[Unit] =
    (skipHorizontalWhitespace ~> char(s) <~ skipHorizontalWhitespace).void

  def hexStr: Parser[String] =
    many(hexDigit).map(_.mkString)

  def hexStrN(n: Int): Parser[String] =
    manyN(n, hexDigit).map(_.mkString)

  def uuid: Parser[UUID] = (
    hexStrN( 8) <~ char('-') |@|
    hexStrN( 4) <~ char('-') |@|
    hexStrN( 4) <~ char('-') |@|
    hexStrN( 4) <~ char('-') |@|
    hexStrN(12)
  ) { (a, b, c, d, e) ⇒ UUID.fromString(s"$a-$b-$c-$d-$e") }

}
