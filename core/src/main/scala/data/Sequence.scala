package es.uvigo.ei.sing.funpep
package data

import scalaz._
import scalaz.std.string._
import scalaz.syntax.show._

final case class Sequence[A](
  header:   Sequence.Header,
  residues: IList[A]
) {

  def identifier: Maybe[String] =
    header.words.headMaybe

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.NoNeedForMonad"))
  // NoNeedForMonad false positive, probably related to wartremover#144, see:
  // https://github.com/puffnfresh/wartremover/pull/144#issuecomment-77609330
  //
  // This fails:
  // def f(x: Int): Option[Int] = Option(x * 2)
  // List(1, 2, 3).headOption.flatMap(f)
  //
  // This does not:
  // val f: Int ⇒ Option[Int] = x ⇒ Option(x * 2)
  // List(1, 2, 3).headOption.flatMap(f)
  //
  // Alternative for-notation does not fail, but incurs in an unnecessary
  // map(identity). Another non-error alternative is to use scalaz bind
  // operators (>>=, ∗), but seems like a nasty hack to prevent a warning, I do
  // prefer @SuppressWarnings in this case.
  //
  // XXX: Current wartremover version is 0.13. Remove warning suppression
  // whenever it gets fixed and published in stable release.
  def comment: Maybe[String] =
    header.words.tailMaybe.flatMap(_.mkString(" "))

  def toString(lineLength: Int)(implicit ev: Show[A]): String = {
    def line(header: String): String =
      header + " " + Stream.continually('-').take(lineLength - header.length - 1).mkString + "\n"

    def format(str: String): String =
      str.grouped(lineLength).mkString("\n")

    line("ID")       + format(~identifier) + "\n" ++
    line("Comment")  + format(~comment)    + "\n" ++
    line("Residues") + residuesToString(lineLength)
  }

  def residuesToString(lineLength: Int)(implicit ev: Show[A]): String =
    residues.map(_.shows).foldRight("")(_ + _).grouped(lineLength).mkString("\n")

}

object Sequence {

  type Header = String

  implicit def SequenceEqual[A: Equal]: Equal[Sequence[A]] =
    implicitly[Equal[IList[A]]].contramap(_.residues)

  implicit def SequenceShow[A: Show]: Show[Sequence[A]] =
    Show.shows(_.toString(60))

}
