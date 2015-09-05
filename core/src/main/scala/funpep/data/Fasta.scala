package funpep
package data

import java.nio.file.Path

import scalaz._
import scalaz.syntax.equal._

import scalaz.concurrent._
import scalaz.stream._

import atto._

import util.ops.foldable._
import util.types._


final case class Fasta[A] (
  val entries: NonEmptyList[Sequence[A]]
)(implicit ev: A ⇒ Compound) {

  def toString(length: Int): String = {
    def format(seq: Sequence[A]): String =
      ">" + seq.header + "\n" + seq.residues.mkString(_.code.toString).grouped(length).mkString("\n")

    entries.map(format).mkString(identity, "\n")
  }

  def toFile(path: Path): Process[Task, Unit] =
    Process(toString(70)).pipe(text.utf8Encode).to(nio.file.chunkW(path))

}

object Fasta {

  def apply[A](head: Sequence[A], tail: Sequence[A]*)(implicit ev: A ⇒ Compound): Fasta[A] =
    new Fasta(NonEmptyList(head, tail: _*))

  implicit def FastaEqual[A: Equal]: Equal[Fasta[A]] =
    Equal.equal(_.entries ≟ _.entries)

  implicit def FastaShow[A]: Show[Fasta[A]] =
    Show.shows(_.toString(70))

}

final class FastaParser[A] private[data] (val compound: Parser[A])(implicit ev: A ⇒ Compound) {

  import java.nio.channels.AsynchronousFileChannel

  import scalaz.syntax.applicative._
  import scalaz.syntax.foldable._

  import atto.parser.character._
  import atto.parser.combinator._
  import atto.parser.text._
  import atto.syntax.parser._
  import atto.syntax.stream.all._

  lazy val fasta:    Parser[Fasta[A]]    = many1(sequence).map(ss ⇒ Fasta(ss))
  lazy val sequence: Parser[Sequence[A]] = (header |@| residues)(Sequence.apply)

  lazy val header:   Parser[String]   = char('>') ~> takeWhile(c ⇒ c != '\r' && c != '\n') <~ eol
  lazy val residues: Parser[IList[A]] = many1(residuesLine).map(_.toIList.flatten)

  lazy val residuesLine: Parser[IList[A]] = (many1(compound) <~ (eol | eoi)).map(_.toIList)

  lazy val eoi: Parser[Unit] = atto.parser.combinator.endOfInput
  lazy val eol: Parser[Unit] = (cr | lf | cr ~ lf) map { _ ⇒ () }
  lazy val cr:  Parser[Char] = char(0x0D)
  lazy val lf:  Parser[Char] = char(0x0A)

  def fromString(str: String): ErrorMsg ∨ Fasta[A] =
    fasta.parseOnly(str).either

  def fromFile(path: Path): Process[Task, ErrorMsg ∨ Fasta[A]] =
    nio.file.textR(AsynchronousFileChannel.open(path)).parse1(fasta).map(_.either)

}

object FastaParser {

  def apply[A](implicit parser: Parser[A], ev: A ⇒ Compound): FastaParser[A] =
    new FastaParser[A](parser)

  def parser[A](implicit parser: Parser[A], ev: A ⇒ Compound): Parser[Fasta[A]] =
    new FastaParser[A](parser).fasta

  def fromString[A](str: String)(implicit parser: Parser[A], ev: A ⇒ Compound): ErrorMsg ∨ Fasta[A] =
    FastaParser[A].fromString(str)

  def fromFile[A](path: Path)(implicit parser: Parser[A], ev: A ⇒ Compound): Process[Task, ErrorMsg ∨ Fasta[A]] =
    FastaParser[A].fromFile(path)

}

object FastaPrinter {

  def toString[A](fasta: Fasta[A]): String =
    fasta.toString(70)

  def toFile[A](fasta: Fasta[A], path: Path): Process[Task, Unit] =
    fasta.toFile(path)

  def toStdOut[A](fasta: Fasta[A]): Process[Task, Unit] =
    Process(toString(fasta)).to(io.stdOut)

}
