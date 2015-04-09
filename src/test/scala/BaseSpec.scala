package es.uvigo.ei.sing.funpep

import scalaz._
import scalaz.Scalaz._

import org.specs2._
import org.scalacheck._
import org.scalacheck.util.Pretty

trait Arbitraries {

  import scalaz.scalacheck.ScalazArbitrary._
  import scalaz.scalacheck.ScalaCheckBinding._

  import Arbitrary.arbitrary

  val genFastaEntry = (arbitrary[String] |@| arbitrary[CaseInsensitive[String]])(FastaEntry)
  val genFasta      = arbitrary[NonEmptyList[FastaEntry]] map Fasta

  implicit lazy val arbitraryFastaEntry: Arbitrary[FastaEntry] = Arbitrary(genFastaEntry)
  implicit lazy val arbitraryFasta:      Arbitrary[Fasta]      = Arbitrary(genFasta)

}

trait Pretties {

  implicit def prettyFastaEntry(entry: FastaEntry): Pretty =
    Pretty { _ ⇒ s"FastaEntry(id = ${entry.id}, seq = ${entry.seq.shows})" }

  implicit def prettyFasta(fasta: Fasta): Pretty =
    Pretty { _ ⇒ s"Fasta(entries = ${fasta.entries.shows})" }

}

trait BaseSpec extends Specification with ScalaCheck with Arbitraries with Pretties {

  def ∀[A](property: ⇒ A ⇒ Boolean)(implicit a: Arbitrary[A], s: Shrink[A], p: A ⇒ Pretty): Prop =
    Prop.forAll(property)

  def ∃[A](property: ⇒ A ⇒ Boolean)(implicit a: Arbitrary[A], p: A ⇒ Pretty): Prop =
    Prop.exists(property)

}
