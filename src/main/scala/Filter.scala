package es.uvigo.ei.sing.funpep

import java.io.IOException
import java.nio.file.{ Path, Files }

import scalaz.Scalaz._
import scalaz.effect._
import scalaz.iteratee._
import scalaz.iteratee.Iteratee._

import data.{ Fasta, FastaEntry, FastaParser }
import data.Config.syntax._
import contrib.Clustal
import util.IOUtils._


object Filter {

  // TODO: clean up
  def filterSimilarEntries(directory: Path, threshold: Double): ConfiguredT[⇄, List[FastaEntry]] = {
    lazy val files = FastaParser.fromDirectory(directory).liftM[ConfiguredT]

    // Type ascription required because Scala does not correctly infer it here
    lazy val entries: ConfiguredT[⇄, List[(FastaEntry, Boolean)]] = files >>= (_ map {
      f ⇒ isEntrySimilarTo(f._2, f._1.entries.head, threshold).map((f._1.entries.head, _))
    } sequenceU)

    entries.map(_ collect { case (entry, true) ⇒ entry })
  }

  def isEntrySimilarTo(fasta: Path, entry: FastaEntry, threshold: Double): ConfiguredT[⇄, Boolean] =
    Clustal.withDistanceMatrixOf(fasta)(isEntrySimilarEnough(entry, threshold))

  def maxDistMatValue(distMat: Path, entry: FastaEntry): ⇄[Double] =
    distMat.enumerateLines(findDistMatEntry(entry)) map { line ⇒
      val dist = line >>= { maxDistMatLineValue(_, entry) }
      dist \/> new IOException(s"Could not found maximum distance of ${entry.id} in $distMat")
    }

  def maxDistMatLineValue(distMatLine: String, entry: FastaEntry): Option[Double] = {
    val dists = distMatLine.drop(entry.id.size + 12).split("\\s+").toList
    dists traverse (_.parseDouble.toOption) map (_.max)
  }

  def findDistMatEntry(entry: FastaEntry): IterateeT[IoExceptionOr[String], IO, Option[String]] =
    for {
      _ ← dropUntil[IoExceptionOr[String], IO](_ exists (_ startsWith entry.id))
      h ← head[IoExceptionOr[String], IO]
    } yield h.map(_.toOption).flatten

  private def isEntrySimilarEnough(entry: FastaEntry, threshold: Double)(distMat: Path): ⇄[Boolean] =
    (maxDistMatValue(distMat, entry) <* distMat.delete) map { _ >= threshold }

}
