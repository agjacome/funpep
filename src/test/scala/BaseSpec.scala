package es.uvigo.ei.sing.funpep

import scalaz._
import scalaz.Scalaz._

import org.specs2._
import org.scalacheck._

trait Arbitraries {

  import scalaz.scalacheck.ScalazArbitrary._
  import scalaz.scalacheck.ScalaCheckBinding._

  import Arbitrary.arbitrary

  val GenFastaEntry = (arbitrary[String] |@| arbitrary[CaseInsensitive[String]])(FastaEntry)
  val GenFasta      = arbitrary[NonEmptyList[FastaEntry]] map Fasta

  implicit lazy val ArbitraryFastaEntry: Arbitrary[FastaEntry] = Arbitrary(GenFastaEntry)
  implicit lazy val ArbitraryFasta:      Arbitrary[Fasta]      = Arbitrary(GenFasta)

}

trait Pretties {

  import org.scalacheck.util.Pretty

  implicit def prettyFastaEntry(entry: FastaEntry): Pretty =
    Pretty { _ ⇒ ¶ + entry.shows }

  implicit def prettyFasta(fasta: Fasta): Pretty =
    Pretty { _ ⇒ ¶ + fasta.shows }

}

trait BaseSpec extends Specification with ScalaCheck with Arbitraries with Pretties
