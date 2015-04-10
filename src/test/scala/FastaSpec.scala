package es.uvigo.ei.sing.funpep

import scalaz.Scalaz._
import scalaz.scalacheck.ScalazProperties._


class `FastaEntry Specification` extends BaseSpec { def is = s2"""

  satisfies equality laws (Equal[FastaEntry]):
    commutative $testEqualCommutative
    reflexive   $testEqualReflexive
    transitive  $testEqualTransitive

  can be presented as a String (Show[FastaEntry]) $testShowInstance

"""

  lazy val testEqualCommutative = equal.commutativity[FastaEntry]
  lazy val testEqualReflexive   = equal.reflexive[FastaEntry]
  lazy val testEqualTransitive  = equal.transitive[FastaEntry]

  lazy val testShowInstance = ∀[FastaEntry] { e ⇒ e.shows ≟ entryToString(e) }

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

  lazy val testEqualCommutative = equal.commutativity[Fasta]
  lazy val testEqualReflexive   = equal.reflexive[Fasta]
  lazy val testEqualTransitive  = equal.transitive[Fasta]

  lazy val testSemigroupAssociative = semigroup.associative[Fasta]

  lazy val testShowInstance = ∀[Fasta] { f ⇒ f.shows ≟ fastaToString(f) }

  private def fastaToString(fasta: Fasta): String =
    fasta.entries.map(_.shows).toList.mkString(¶)

}

class `FastaParser Specification` extends BaseSpec { def is = s2"""

  parses Fastas from Strings $testFromString
  parses Fastas from Readers $testFromReader

"""

  import java.io.{ BufferedReader, StringReader }

  lazy val testFromString = ∀[Fasta] {
    fasta ⇒ FastaParser.fromString(fasta.shows).exists(_ ≟ fasta)
  }

  lazy val testFromReader = ∀[Fasta] { fasta ⇒
    val reader = new BufferedReader(new StringReader(fasta.shows))
    reader.bracket(_.close) { br ⇒ FastaParser.fromReader(br).exists(_ ≟ fasta) }
  }

}
