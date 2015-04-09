package es.uvigo.ei.sing.funpep

import scalaz.Scalaz._
import scalaz.scalacheck.ScalazProperties._

import org.scalacheck.Prop

class `FastaEntry Specification` extends BaseSpec { def is = s2"""
 
  satisfies equality laws (Equal[FastaEntry]):
    commutative $testEqualCommutative
    reflexive   $testEqualReflexive
    transitive  $testEqualTransitive

  can be presented as a String (Show[FastaEntry]) $testShowInstance

"""

  def testEqualCommutative: Prop = equal.commutativity[FastaEntry]
  def testEqualReflexive:   Prop = equal.reflexive[FastaEntry]
  def testEqualTransitive:  Prop = equal.transitive[FastaEntry]

  def testShowInstance: Prop = ∀[FastaEntry] { e ⇒ e.shows ≟ entryToString(e) }

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

  def testEqualCommutative: Prop = equal.commutativity[Fasta]
  def testEqualReflexive:   Prop = equal.reflexive[Fasta]
  def testEqualTransitive:  Prop = equal.transitive[Fasta]

  def testSemigroupAssociative: Prop = semigroup.associative[Fasta]

  def testShowInstance: Prop = ∀[Fasta] { f ⇒ f.shows ≟ fastaToString(f) }

  private def fastaToString(fasta: Fasta): String =
    fasta.entries.map(_.shows).toList.mkString(¶)


}
