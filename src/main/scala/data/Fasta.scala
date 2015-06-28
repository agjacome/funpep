package es.uvigo.ei.sing.funpep
package data

import java.io.{ BufferedReader, BufferedWriter }
import java.nio.file.Path

import scala.util.parsing.combinator.RegexParsers

import scalaz._
import scalaz.Scalaz._
import scalaz.effect._

import argonaut._
import argonaut.Argonaut._

import util.IOUtils._


final case class FastaEntry (id: FastaEntry.ID, seq: CaseInsensitive[String]) {

  lazy val toEntryString: String = ">" + id + nl + seq.original.grouped(70).mkString(nl)

}

object FastaEntry {

  type ID = String

  implicit val FastaEntryInstances = new Equal[FastaEntry] with Show[FastaEntry] {
    override def equal(e1: FastaEntry, e2: FastaEntry): Boolean =
      e1.seq ≟ e2.seq

    override def show(e: FastaEntry): Cord =
      Cord(e.toEntryString)
  }

}

final case class Fasta (entries: NonEmptyList[FastaEntry]) {

  import Fasta.FastaInstances

  lazy val toFastaString: String = entries.map(_.toEntryString).toList.mkString(nl)

  def filter(cond: FastaEntry ⇒ Boolean): Option[Fasta] =
    entries.toList.filter(cond).toNel.map(Fasta.apply)

  def find(cond: FastaEntry ⇒ Boolean): Option[FastaEntry] =
    entries.toStream.find(cond)

  def exists(cond: FastaEntry ⇒ Boolean): Boolean =
    entries.toStream.exists(cond)

  // aliases for FastaPrinter.to*
  def toFile   (file: Path): IOThrowable[Unit] = FastaPrinter.toFile(file)(this)
  def toNewFile(dir:  Path): IOThrowable[Unit] = FastaPrinter.toNewFile(dir)(this)

}

object Fasta {

  def apply(e: FastaEntry, es: FastaEntry*): Fasta =
    new Fasta(NonEmptyList(e, es: _*))

  // aliases for FastaParser.from*
  def apply(str:  String): Throwable ∨ Fasta = FastaParser.fromString(str)
  def apply(file: Path  ): IOThrowable[Fasta]  = FastaParser.fromFile(file)

  implicit val FastaInstances = new Equal[Fasta] with Show[Fasta] with Semigroup[Fasta] {
    override def equal(f1: Fasta, f2: Fasta): Boolean =
      f1.entries ≟ f2.entries

    override def show(f: Fasta): Cord =
      Cord(f.toFastaString)

    override def append(f1: Fasta, f2: ⇒ Fasta): Fasta =
      Fasta(f1.entries.append(f2.entries))
  }

  implicit val FastaDecodeJson: DecodeJson[Fasta] =
    optionDecoder(_.string >>= { str ⇒ Fasta.apply(str).toOption }, "Email")

  implicit val FastaEncodeJson: EncodeJson[Fasta] =
    StringEncodeJson.contramap(_.shows)

}

object FastaParser extends RegexParsers {

  lazy val header   = """>.*""".r    ^^ { _.tail.trim }
  lazy val seqLine  = """[^>].*""".r ^^ { _.trim      }
  lazy val sequence = seqLine.+      ^^ { _.mkString  }

  lazy val entry = header ~ sequence ^^ { e ⇒ FastaEntry(e._1, e._2.uncased) }
  lazy val fasta = entry.+           ^^ { f ⇒ f.toNel.map(Fasta.apply)       }

  lazy val parseString: String         ⇒ ParseResult[Option[Fasta]] = parseAll(fasta, _)
  lazy val parseReader: BufferedReader ⇒ ParseResult[Option[Fasta]] = parseAll(fasta, _)

  lazy val fromString: String         ⇒ Throwable ∨ Fasta = parseString ∘ validate
  lazy val fromReader: BufferedReader ⇒ Throwable ∨ Fasta = parseReader ∘ validate

  def fromFile(file: Path): IOThrowable[Fasta] =
    EitherT { file.openIOReader.bracket(_.closeIO)(r ⇒ fromReader(r).point[IO]).catchLeft map (_.join) }

  def fromDirectory(directory: Path): IOThrowable[List[(Fasta, Path)]] =
    directory.files("*.{fasta,fas,fna,faa,ffn,frna}") >>= {
      files ⇒ files.traverse(fromFile).map(_.zip(files))
    }

  private def validate(res: ParseResult[Option[Fasta]]): Throwable ∨ Fasta =
    res.getOrElse(None) \/> new IllegalArgumentException("Could not parse content as FASTA")

}

object FastaPrinter {

  import scalaz.\/.{ fromTryCatchNonFatal ⇒ tryCatch }

  lazy val toWriter: BufferedWriter ⇒ Fasta ⇒ Throwable ∨ Unit =
    writer ⇒ fasta ⇒ tryCatch { writer.write(fasta.toFastaString) }

  def toFile(file: Path)(fasta: ⇒ Fasta): IOThrowable[Unit] =
    file.openIOWriter.bracket(_.closeIO) { toWriter(_)(fasta).point[IO] }

  def toNewFile(directory: Path)(fasta: ⇒ Fasta): IOThrowable[Unit] =
    toFile(directory / uuid.toString + ".fasta")(fasta)

  def toDirectory(directory: Path)(fastas: ⇒ List[Fasta]): IOThrowable[Unit] =
    fastas traverse { f ⇒ toNewFile(directory)(f) } map { _ ⇒ () }

}
