package es.uvigo.ei.sing.funpep

import java.nio.file.Path

import scalaz.Scalaz._

import contrib.Clustal
import data._
import data.Config._
import util.IOUtils._


// TODO: ugly code, clean up
// Buggy wartremover warnings on pattern matching
@SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Throw"))
object Reporter {

  type LineNumber   = Int
  type ParsedLine   = (FastaEntry.ID, LineNumber, List[Double])
  type SimilarLine  = (FastaEntry.ID, LineNumber, Double)
  type SimilarEntry = (FastaEntry.ID, FastaEntry.ID, Double)

  lazy val csvHeader = """"Comparing ID","Comparing Sequence","Reference ID","Reference Sequence","Similarity Percentage""""

  def generateReport(reference: Path, filtered: Path): ConfiguredT[IOThrowable, String] =
    for {
      ref ← Fasta(reference).liftM[ConfiguredT]
      fil ← Fasta(filtered).liftM[ConfiguredT]
      csv ← generateReportLines(ref, fil, filtered)
    } yield (csvHeader :: csv.orZero.dropRight(ref.entries.size)).mkString(nl)

  def generateReportLines(reference: Fasta, filtered: Fasta, filterPath: Path): ConfiguredT[IOThrowable, Option[List[String]]] = {
    val simEntries = distanceMatrixLines(filterPath) map {
      similarities(_, reference.entries.size) map (ss ⇒ ss map {
        case (entry, idx, max) ⇒ (entry, ss(idx)._1, max)
      })
    }

    simEntries map (_ >>= { _ traverse (reportLine(_, filtered)) })
  }

  def similarities(distMatLines: List[String], refSize: Int): Option[List[SimilarLine]] =
    distMatLines.zipWithIndex.traverse(parseDistMatLine).map(_ map {
      case (entry, index, dists) ⇒
        val similarities   = dists.updated(index, Double.MinValue).takeRight(refSize)
        val maxSimilarity  = similarities.max
        val mostSimilarIdx = dists.indexOf(maxSimilarity, dists.size - refSize)

        (entry, mostSimilarIdx, maxSimilarity)
    })

  def reportLine(simEntry: SimilarEntry, filtered: Fasta): Option[String] = {
    val (e1, e2, num) = simEntry

    // Need to use "startsWith" instead of "===" because Clustal drops
    // everything from first space to end in each entry ID
    (filtered.find(_.id startsWith e1) |@| filtered.find(_.id startsWith e2)) {
      (s1, s2) ⇒ s""""$e1","${s1.seq.original}","$e2","${s2.seq.original}","$num""""
    }
  }

  private def parseDistMatLine(line: (String, Int)): Option[ParsedLine] = {
    val split = line._1.split("\\s+").toList

    (split.headOption |@| split.drop(1).traverse(_.parseDouble.toOption)) {
      (entry, dists) ⇒ (entry, line._2, dists)
    }
  }

  private def distanceMatrixLines(fastaFile: Path): ConfiguredT[IOThrowable, List[String]] =
    Clustal.withDistanceMatrixOf(fastaFile) {
      distMat ⇒ (distMat.contentsAsList.map(_.drop(1)) <* distMat.delete)
    }

}
