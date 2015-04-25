package es.uvigo.ei.sing.funpep
package data

import java.io.{ BufferedReader, StringReader }
import java.nio.file.{ Files, Path }

import scalaz.Scalaz._
import scalaz.effect.IO
import scalaz.scalacheck.ScalazProperties._

import util.IOUtils._

class `FastaEntry Specification` extends BaseSpec { def is = s2"""

  satisfies equality laws (Equal[FastaEntry]):
    commutative $testEqualCommutative
    reflexive   $testEqualReflexive
    transitive  $testEqualTransitive

  can be presented as a String (Show[FastaEntry]) $testShowInstance

"""

  def testEqualCommutative = equal.commutativity[FastaEntry]
  def testEqualReflexive   = equal.reflexive[FastaEntry]
  def testEqualTransitive  = equal.transitive[FastaEntry]

  def testShowInstance = ∀[FastaEntry] { e ⇒ e.shows ≟ entryToString(e) }

  private def entryToString(entry: FastaEntry): String =
    ">" + entry.id + ¶ + entry.seq.original.grouped(70).mkString(¶)

}

class `Fasta Specification` extends BaseSpec { def is = s2"""

  satisfies equality laws (Equal[Fasta]):
    commutative $testEqualCommutative
    reflexive   $testEqualReflexive
    transitive  $testEqualTransitive

  satisfies semigroup laws (Semigroup[Fasta]):
    associative $testSemigroupAssociative

  can be presented as a String (Show[Fasta]) $testShowInstance

"""

  def testEqualCommutative = equal.commutativity[Fasta]
  def testEqualReflexive   = equal.reflexive[Fasta]
  def testEqualTransitive  = equal.transitive[Fasta]

  def testSemigroupAssociative = semigroup.associative[Fasta]

  def testShowInstance = ∀[Fasta] { f ⇒ f.shows ≟ fastaToString(f) }

  private def fastaToString(fasta: Fasta): String =
    fasta.entries.map(_.shows).toList.mkString(¶)

}

class `FastaParser Specification` extends BaseSpec { def is = s2"""

  can correctly parse FASTAs:
    from Strings     $testFromString
    from Readers     $testFromReader
    from a file      $testFromFile
    from a directory $testFromDirectory

"""

  def testFromString = ∀[Fasta] {
    fasta ⇒ FastaParser.fromString(fasta.shows).exists(_ ≟ fasta)
  }

  def testFromReader = ∀[Fasta] { fasta ⇒
    val reader = new BufferedReader(new StringReader(fasta.shows))
    reader.bracket(_.close) {
      FastaParser.fromReader(_).exists(_ ≟ fasta)
    }
  }

  def testFromFile = ∀[Fasta] { fasta ⇒
    val file = newTemporalFile(".fasta")

    lazy val write = writeFasta(file, fasta)
    lazy val read  = FastaParser.fromFile(file) exists {
      _ ≟ fasta
    }

    (write *> read).unsafePerformIO
  }

  def testFromDirectory = ∀[List[Fasta]] { fastas ⇒
    val directory = newTemporalDirectory()

    lazy val write = fastas map {
      fasta ⇒ writeFasta(newTemporalFileIn(directory, ".fasta"), fasta)
    } sequence

    lazy val read = FastaParser.fromDirectory(directory) exists {
      parsed ⇒ parsed.size ≟ fastas.size &&
               parsed.filter(t ⇒ fastas.contains(t._1)).size ≟ fastas.size
    }

    (write *> read).unsafePerformIO
  }

  private def writeFasta(file: Path, fasta: Fasta): IO[Unit] =
    file.openIOWriter.bracket(_.closeIO)(_.writeIO(fasta.shows))

}
