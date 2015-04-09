package es.uvigo.ei.sing.funpep

import scalaz._
import scalaz.Scalaz._
import scalaz.scalacheck.ScalazProperties._

import org.scalacheck.Prop, Prop.{ forAll ⇒ ∀ }

class `FastaEntry Specification` extends BaseSpec { def is = s2"""
 
  satisfies equality laws (Equal[FastaEntry]):
    commutative ${ equal.commutativity[FastaEntry] }
    reflexive   ${ equal.reflexive[FastaEntry]     }
    transitive  ${ equal.transitive[FastaEntry]    }

  can be presented as a String (Show[FastaEntry]) $testShowInstance

"""

  def testShowInstance: Prop = ∀ {
    (entry: FastaEntry) ⇒ Show[FastaEntry].shows(entry) ≟ entryToString(entry)
  }

  val entryToString: FastaEntry ⇒ String =
    entry ⇒ ">" + entry.id + ¶ + entry.seq.original.grouped(70).mkString(¶)

}

class `Fasta Specification` extends BaseSpec { def is = s2"""
  
  satisfies equality laws (Equal[Fasta]):
    commutative ${ equal.commutativity[Fasta] }
    reflexive   ${ equal.reflexive[Fasta]     }
    transitive  ${ equal.transitive[Fasta]    }

  satisfies semigroup laws (Semigroup[Fasta]):
    associative ${ semigroup.associative[Fasta] }

  can be presented as a String (Show[Fasta]) $testShowInstance

"""

  val fastaToString: Fasta ⇒ String =
    fasta ⇒ fasta.entries.map(_.shows).toList.mkString(¶)

  def testShowInstance: Prop = ∀ {
    (fasta: Fasta) ⇒ Show[Fasta].shows(fasta) ≟ fastaToString(fasta)
  }

}
