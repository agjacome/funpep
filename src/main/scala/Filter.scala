package es.uvigo.ei.sing.funpep

import java.io.IOException
import java.nio.file.{ Path, Files }

import scalaz._
import scalaz.Scalaz._
import scalaz.effect._
import scalaz.iteratee._
import scalaz.iteratee.Iteratee._

import Config._


object Filter {

  // TODO: clean up
  def filterSimilarEntries(directory: Path, threshold: Double): ConfiguredT[ErrorOrIO, List[FastaEntry]] = {
    val files = ConfiguredT { _ ⇒ FastaParser.fromDirectory(directory) }

    val entries: ConfiguredT[ErrorOrIO, List[(FastaEntry, Boolean)]] = files >>= {
      _.map(f ⇒ isEntrySimilarTo(f._2, f._1.entries.head, threshold).map((f._1.entries.head, _))).sequenceU
    }

    entries map { _.filter(_._2).map(_._1) }
  }

  def isEntrySimilarTo(fasta: Path, entry: FastaEntry, threshold: Double): ConfiguredT[ErrorOrIO, Boolean] =
    Clustal.withDistanceMatrixOf(fasta)(isEntrySimilarEnough(entry, threshold))

  def maxDistMatValue(distMat: Path, entry: FastaEntry): ErrorOrIO[Double] =
    EitherT(distMat.enumerateLines(findDistMatEntry(entry)) map { line ⇒
      val dist = line >>= { maxDistMatLineValue(_, entry) }
      dist \/> new IOException(s"Could not found maximum distance of ${entry.id} in $distMat")
    })

  def maxDistMatLineValue(distMatLine: String, entry: FastaEntry): Option[Double] = {
    val dists = distMatLine.drop(entry.id.size + 12).split("\\s+").toList
    dists traverse (_.parseDouble.toOption) map (_.max)
  }

  def findDistMatEntry(entry: FastaEntry): IterateeT[IoExceptionOr[String], IO, Option[String]] =
    for {
      _ ← dropUntil[IoExceptionOr[String], IO](_ exists (_ startsWith entry.id))
      h ← head[IoExceptionOr[String], IO]
    } yield h.map(_.toOption).flatten

  private def isEntrySimilarEnough(entry: FastaEntry, threshold: Double)(distMat: Path): ErrorOrIO[Boolean] =
    (maxDistMatValue(distMat, entry) <* distMat.delete) map { _ >= threshold }

}
