package es.uvigo.ei.sing.funpep

import java.io.IOException
import java.nio.file.{ Path, Files }

import scala.collection.parallel.ParSeq

import scalaz.Scalaz._
import scalaz.effect._
import scalaz.iteratee._
import scalaz.iteratee.Iteratee._

import data._
import data.Config._
import contrib.Clustal
import util.IOUtils._


// TODO: find a way to parallelize, N files at a time
object Filter {

  // FIXME: Deplorable
  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Throw"))
  def parFilterSimilarEntries(directory: Path, threshold: Double)(config: Config): IOThrowable[List[FastaEntry]] = {
    val files = FastaParser.fromDirectory(directory).getOrElse(List.empty[(Fasta, Path)]).unsafePerformIO()

    val entries = files.par map { case (fasta, path) ⇒
      val io = isEntrySimilarTo(path, fasta.entries.head, threshold).map(_.option(fasta.entries.head))
      io.apply(config).getOrElse(none[FastaEntry]).unsafePerformIO()
    }

    entries.toList.map(_.toList).flatten.right[Throwable].point[IO]
  }

  def filterSimilarEntries(directory: Path, threshold: Double): ConfiguredT[IOThrowable, List[FastaEntry]] =
    FastaParser.fromDirectory(directory).liftM[ConfiguredT].map(_.toList) >>= {
      filterSimilarFastas(threshold)
    }

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Throw"))
  def filterSimilarFastas(threshold: Double)(files: List[(Fasta, Path)]): ConfiguredT[IOThrowable, List[FastaEntry]] =
    (files map { case (fasta, path) ⇒
      isEntrySimilarTo(path, fasta.entries.head, threshold).map(_.option(fasta.entries.head))
    }).sequenceU map { _.map(_.toList).flatten }

  def isEntrySimilarTo(fasta: Path, entry: FastaEntry, threshold: Double): ConfiguredT[IOThrowable, Boolean] =
    Clustal.withDistanceMatrixOf(fasta)(isEntrySimilarEnough(entry, threshold))

  def maxDistMatValue(distMat: Path, entry: FastaEntry): IOThrowable[Double] =
    distMat.enumerateLines(findDistMatEntry(entry)) map { line ⇒
      val dist = line >>= { maxDistMatLineValue(_, entry) }
      dist \/> new IOException(s"Could not found maximum distance of ${entry.id} in $distMat")
    }

  def maxDistMatLineValue(distMatLine: String, entry: FastaEntry): Option[Double] = {
    val dists = distMatLine.drop(entry.id.size + 12).split("\\s+").toList
    dists traverse (_.parseDouble.toOption) map (_.max)
  }

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.NoNeedForMonad"))
  def findDistMatEntry(entry: FastaEntry): IterateeT[IoExceptionOr[String], IO, Option[String]] =
    for {
      _ ← dropUntil[IoExceptionOr[String], IO](_ exists (_ startsWith entry.id))
      h ← head[IoExceptionOr[String], IO]
    } yield h.map(_.toOption).flatten

  private def isEntrySimilarEnough(entry: FastaEntry, threshold: Double)(distMat: Path): IOThrowable[Boolean] =
    (maxDistMatValue(distMat, entry) <* distMat.delete) map { _ >= threshold }

}
