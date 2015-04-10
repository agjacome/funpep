package es.uvigo.ei.sing.funpep

import java.nio.file.{ Path, Files }

import scalaz._
import scalaz.Scalaz._

import org.specs2._
import org.scalacheck._
import org.scalacheck.util.Pretty


trait Arbitraries {

  import scalaz.scalacheck.ScalazArbitrary._
  import scalaz.scalacheck.ScalaCheckBinding._

  import Arbitrary.arbitrary
  import Gen.{ alphaChar, nonEmptyListOf }

  val nonEmptyStr   = nonEmptyListOf(alphaChar).map(_.mkString)
  val nonEmptyCIStr = nonEmptyStr.map(str ⇒ CaseInsensitive(str))

  val genFastaEntry = (nonEmptyStr |@| nonEmptyCIStr)(FastaEntry)
  val genFasta      = arbitrary[NonEmptyList[FastaEntry]] map Fasta

  implicit lazy val arbitraryFastaEntry: Arbitrary[FastaEntry] = Arbitrary(genFastaEntry)
  implicit lazy val arbitraryFasta:      Arbitrary[Fasta]      = Arbitrary(genFasta)

}

trait Pretties {

  implicit def prettyFastaEntry(entry: FastaEntry): Pretty =
    Pretty { _ ⇒ s"FastaEntry(id = ${entry.id}, seq = ${entry.seq.shows})" }

  implicit def prettyFasta(fasta: Fasta): Pretty =
    Pretty { _ ⇒ s"Fasta(entries = ${fasta.entries.shows})" }

  implicit def prettyList[A: Show](xs: List[A]): Pretty =
    Pretty { _ ⇒ xs.shows }

}

trait BaseSpec extends Specification with ScalaCheck with Arbitraries with Pretties {

  def ∀[A: Arbitrary: Shrink](property: ⇒ A ⇒ Boolean)(implicit pretty: A ⇒ Pretty): Prop =
    Prop.forAll(property)

  def ∃[A: Arbitrary](property: ⇒ A ⇒ Boolean)(implicit pretty: A ⇒ Pretty): Prop =
    Prop.exists(property)


  def newTemporalFile(suffix: String): Path = {
    val path = Files.createTempFile("funpep-test-", suffix)
    path.toFile.deleteOnExit()
    path
  }

  def newTemporalFileIn(dir: Path, suffix: String): Path = {
    val path = Files.createTempFile(dir, "funpep-test-", suffix)
    path.toFile.deleteOnExit()
    path
  }

  def newTemporalDirectory(): Path = {
    val path = Files.createTempDirectory("funpep-testdir-")
    path.toFile.deleteOnExit()
    path
  }

}
