package es.uvigo.ei.sing.funpep

import java.io.{ BufferedReader, BufferedWriter }
import java.nio.file.Path

import scala.util.parsing.combinator.RegexParsers

import scalaz._
import scalaz.Scalaz._
import scalaz.effect._


final case class FastaEntry (id: FastaEntry.ID, seq: CaseInsensitive[String])

object FastaEntry extends ((String, CaseInsensitive[String]) ⇒ FastaEntry) {

  type ID = String

  implicit val equal = new Equal[FastaEntry] {
    override def equal(e1: FastaEntry, e2: FastaEntry): Boolean =
      e1.seq ≟ e2.seq
  }

  implicit val show = new Show[FastaEntry] {
    override def show(e: FastaEntry): Cord =
      Cord(">", e.id, ¶, e.seq.original grouped 70 mkString ¶)
  }

}

final case class Fasta (entries: NonEmptyList[FastaEntry])

object Fasta extends (NonEmptyList[FastaEntry] ⇒ Fasta) {

  import Cord.mkCord

  implicit val show = new Show[Fasta] {
    override def show(f: Fasta): Cord =
      mkCord(¶, f.entries map (_.show) toList: _*)
  }

}

object FastaParser extends RegexParsers {

  type Parsed[A] = Throwable \/ A

  lazy val header   = """>.*""".r    ^^ { _.tail.trim }
  lazy val seqLine  = """[^>].*""".r ^^ { _.trim      }
  lazy val sequence = seqLine.+      ^^ { _.mkString  }

  lazy val entry = header ~ sequence ^^ { e ⇒ FastaEntry(e._1, e._2) }
  lazy val fasta = entry.+           ^^ { f ⇒ f.toNel map Fasta      }

  val parseString: String         ⇒ ParseResult[Option[Fasta]] = parseAll(fasta, _)
  val parseReader: BufferedReader ⇒ ParseResult[Option[Fasta]] = parseAll(fasta, _)

  val validate: ParseResult[Option[Fasta]] ⇒ Parsed[Fasta] =
    _.getOrElse(None) \/> new Throwable("Could not parse content as FASTA")

  val fromReader: BufferedReader ⇒ IO[Parsed[Fasta]] =
    (parseReader ∘ validate)(_).point[IO].catchLeft map (_.join)

  val fromFile: Path ⇒ IO[Parsed[Fasta]] =
    _.openIOReader.bracket(_.closeIO)(fromReader)

  val fromString: String ⇒ Parsed[Fasta] =
    parseString ∘ validate

  val fromDirectory: Path ⇒ IO[Parsed[List[Fasta]]] =
    _.files("*.{fasta,fas,fna,faa,ffn,frna}") >>= {
      _.map(fromFile).sequence map (_.sequenceU)
    }

}

object FastaPrinter {

  type Printed = Throwable \/ Unit

  def toWriter(fasta: ⇒ Fasta): BufferedWriter ⇒ IO[Printed] =
    _.writeIO(fasta.shows).catchLeft

  def toFile(fasta: ⇒ Fasta): Path ⇒ IO[Printed] =
    _.openIOWriter.bracket(_.closeIO)(toWriter(fasta))

  def toNewFile(fasta: ⇒ Fasta)(dir: Path): IO[Printed] =
    toFile(fasta)(dir / path"$uuid.fasta")

  def toDirectory(fastas: ⇒ List[Fasta])(dir: Path): IO[Printed] =
    fastas.map(toNewFile(_)(dir)).sequence map {
      _.sequenceU map (_ => ())
    }

}
