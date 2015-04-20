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

final case class Fasta (entries: NonEmptyList[FastaEntry]) {

  def filter(cond: FastaEntry ⇒ Boolean): Option[Fasta] =
    entries.toList.filter(cond).toNel.map(Fasta)

  def find(cond: FastaEntry ⇒ Boolean): Option[FastaEntry] =
    entries.toStream.find(cond)

  def exists(cond: FastaEntry ⇒ Boolean): Boolean =
    entries.toStream.exists(cond)

}

object Fasta extends (NonEmptyList[FastaEntry] ⇒ Fasta) {

  import Cord.mkCord

  def apply(e: FastaEntry, es: FastaEntry*): Fasta =
    new Fasta(NonEmptyList(e, es: _*))

  implicit val FastaInstances = new Equal[Fasta] with Show[Fasta] with Semigroup[Fasta] {
    override def equal(f1: Fasta, f2: Fasta): Boolean =
      f1.entries ≟ f2.entries

    override def show(f: Fasta): Cord =
      mkCord(¶, f.entries map (_.show) toList: _*)

    override def append(f1: Fasta, f2: ⇒ Fasta): Fasta =
      Fasta(f1.entries.append(f2.entries))
  }

}

object FastaParser extends RegexParsers {

  lazy val header   = """>.*""".r    ^^ { _.tail.trim }
  lazy val seqLine  = """[^>].*""".r ^^ { _.trim      }
  lazy val sequence = seqLine.+      ^^ { _.mkString  }

  lazy val entry = header ~ sequence ^^ { e ⇒ FastaEntry(e._1, e._2) }
  lazy val fasta = entry.+           ^^ { f ⇒ f.toNel map Fasta      }

  lazy val parseString: String         ⇒ ParseResult[Option[Fasta]] = parseAll(fasta, _)
  lazy val parseReader: BufferedReader ⇒ ParseResult[Option[Fasta]] = parseAll(fasta, _)

  lazy val fromString: String         ⇒ Throwable \/ Fasta = parseString ∘ validate
  lazy val fromReader: BufferedReader ⇒ Throwable \/ Fasta = parseReader ∘ validate

  def fromFile(file: Path): ErrorOrIO[Fasta] =
    EitherT { file.openIOReader.bracket(_.closeIO)(r ⇒ fromReader(r).point[IO]).catchLeft map (_.join) }

  def fromDirectory(directory: Path): ErrorOrIO[List[(Fasta, Path)]] =
    directory.files("*.{fasta,fas,fna,faa,ffn,frna}") >>= {
      files ⇒ (files map fromFile).sequenceU map { _.zip(files) }
    }

  private def validate(res: ParseResult[Option[Fasta]]): Throwable \/ Fasta =
    res.getOrElse(None) \/> new IllegalArgumentException("Could not parse content as FASTA")

}

object FastaPrinter {

  import scalaz.\/.{ fromTryCatchThrowable ⇒ tryCatch }

  type Printed = Throwable \/ Unit

  lazy val toWriter: BufferedWriter ⇒ Fasta ⇒ Printed =
    writer ⇒ fasta ⇒ tryCatch[Unit, Throwable] { writer.write(fasta.shows) }

  def toFile(file: Path)(fasta: ⇒ Fasta): IO[Printed] =
    file.openIOWriter.bracket(_.closeIO) { toWriter(_)(fasta).point[IO] }

  def toNewFile(directory: Path)(fasta: ⇒ Fasta): IO[Printed] =
    toFile(directory / uuid.toPath + ".fasta")(fasta)

  def toDirectory(directory: Path)(fastas: ⇒ List[Fasta]): IO[Printed] =
    fastas.map(f ⇒ toNewFile(directory)(f)).sequence map {
      _.sequenceU map (_ => ())
    }

}
