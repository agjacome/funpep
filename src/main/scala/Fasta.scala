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

  implicit val FastaEntryInstances = new Equal[FastaEntry] with Show[FastaEntry] {
    override def equal(e1: FastaEntry, e2: FastaEntry): Boolean =
      e1.seq ≟ e2.seq

    override def show(e: FastaEntry): Cord =
      Cord(">", e.id, ¶, e.seq.original grouped 70 mkString ¶)
  }

}

final case class Fasta (entries: NonEmptyList[FastaEntry])

object Fasta extends (NonEmptyList[FastaEntry] ⇒ Fasta) {

  import Cord.mkCord

  def apply(e: FastaEntry, es: FastaEntry*): Fasta =
    new Fasta(NonEmptyList(e, es: _*))

  implicit val FastaInstances = new Show[Fasta] {
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

  lazy val parseString: String         ⇒ ParseResult[Option[Fasta]] = parseAll(fasta, _)
  lazy val parseReader: BufferedReader ⇒ ParseResult[Option[Fasta]] = parseAll(fasta, _)

  lazy val fromString: String         ⇒ Parsed[Fasta] = parseString ∘ validate
  lazy val fromReader: BufferedReader ⇒ Parsed[Fasta] = parseReader ∘ validate

  def fromFile(file: Path): IO[Parsed[Fasta]] =
    file.openIOReader.bracket(_.closeIO)(r ⇒ fromReader(r).point[IO]).catchLeft map (_.join)

  def fromDirectory(directory: Path): IO[Parsed[List[Fasta]]] =
    directory.files("*.{fasta,fas,fna,faa,ffn,frna}") >>= {
      _.map(fromFile).sequence map (_.sequenceU)
    }

  private def validate(res: ParseResult[Option[Fasta]]): Parsed[Fasta] =
    res.getOrElse(None) \/> new IllegalArgumentException("Could not parse content as FASTA")

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
