package funpep
package data

import java.nio.file.Path

import scalaz._
import scalaz.syntax.equal._

import scalaz.concurrent._
import scalaz.stream._
import scalaz.syntax.applicative._
import scalaz.syntax.foldable._

import atto._
import atto.parser.character._
import atto.parser.combinator._
import atto.syntax.parser._

import util.functions._
import util.types._
import util.parsers._
import util.ops.disjunction._
import util.ops.foldable._


final case class Fasta[A] (val entries: NonEmptyList[Sequence[A]])(implicit ev: A ⇒ Compound) {

  def toString(length: Int): String = {
    def format(seq: Sequence[A]): String =
      ">" + seq.header + "\n" + seq.residues.mkString(_.code.toString).grouped(length).mkString("\n")

    entries.map(format).mkString(identity, "", "\n", "\n")
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

  implicit def FastaSemigroup[A](implicit ev: A ⇒ Compound): Semigroup[Fasta[A]] =
    new Semigroup[Fasta[A]] {
      override def append(f1: Fasta[A], f2: ⇒ Fasta[A]): Fasta[A] =
        Fasta(Semigroup[NonEmptyList[Sequence[A]]].append(f1.entries, f2.entries))
    }

}

final class FastaParser[A] private[data] (val compound: Parser[A])(implicit ev: A ⇒ Compound) {

  lazy val fasta:    Parser[Fasta[A]]    = many1(sequence).map(ss ⇒ Fasta(ss))
  lazy val sequence: Parser[Sequence[A]] = (header |@| residues)(Sequence.apply)

  lazy val header:   Parser[String]   = char('>') ~> takeLine <~ eol
  lazy val residues: Parser[IList[A]] = many1(residuesLine).map(_.toIList.flatten)

  lazy val residuesLine: Parser[IList[A]] = (many1(compound) <~ (eol | eoi)).map(_.toIList)

  def fromString(str: String): ErrorMsg ∨ Fasta[A] =
    fasta.parseOnly(str).either

  // FIXME: fromFile should be something like:
  //
  //   def fromFile(path: Path): Process[Task, ErrorMsg ∨ Fasta[A]] =
  //     nio.file.textR(path.openAsyncChannel).parse1(fasta).map(_.either)
  //
  // But an OutOfMemoryError is thrown with big enough files implementing it
  // that way. It is also thrown if using a "parser.feed" with each line
  // (folding a Foldable of Strings) instead of creating a big string and using
  // "parseOnly" directly. I can't find the cause ATM, but will try to fix it in
  // a future and stop using the current nonsense.
  def fromFile(path: Path): Process[Task, ErrorMsg ∨ Fasta[A]] =
    textR(path).map(fromString)

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Any"))
  def fromFileW(path: Path): Process[Task, Fasta[A]] =
    fromFile(path) flatMap {
      parsed ⇒ Process.eval(parsed.toTask(identity))
    }

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

  def fromFileW[A](path: Path)(implicit parser: Parser[A], ev: A ⇒ Compound): Process[Task, Fasta[A]] =
    FastaParser[A].fromFileW(path)

}

object FastaPrinter {

  def toString[A](fasta: Fasta[A]): String =
    fasta.toString(70)

  def toFile[A](fasta: Fasta[A], path: Path): Process[Task, Unit] =
    fasta.toFile(path)

  def toStdOut[A](fasta: Fasta[A]): Process[Task, Unit] =
    Process(toString(fasta)).to(io.stdOut)

}
