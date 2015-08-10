package funpep
package data

import scalaz._
import scalaz.std.string._

import funpep.util.ops.string._
import funpep.util.ops.ilist._
import funpep.util.ops.foldable._

final case class Sequence[A](
  header:   Sequence.Header,
  residues: IList[A]
) {

  def identifier: Maybe[String] =
    header.words.headMaybe

  def comment: Maybe[String] =
    header.words.tailMaybe.map(_ mkString " ")

  def toString(lineLength: Int)(implicit ev: Show[A]): String = {
    val dashes = Stream.continually('-')

    def section(title: String): String =
      title + " " + dashes.take(lineLength - title.length - 1).mkString + "\n"

    def format(str: String): String =
      str.grouped(lineLength).mkString("\n")

    section("ID")       + format(~identifier) + "\n" ++
    section("Comment")  + format(~comment)    + "\n" ++
    section("Residues") + format(residues.mkString)
  }

}

object Sequence {

  type Header = String

  implicit def SequenceEqual[A: Equal]: Equal[Sequence[A]] =
    implicitly[Equal[IList[A]]].contramap(_.residues)

  implicit def SequenceShow[A: Show]: Show[Sequence[A]] =
    Show.shows(_.toString(60))

}
