package funpep

import java.nio.file.Path

import scalaz._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.std.list._
import scalaz.syntax.apply._
import scalaz.syntax.traverse._
import scalaz.syntax.std.string._

import data._
import contrib._
import util.types._
import util.ops.disjunction._
import util.ops.path._


object SimilarityFilter {

  def apply[A](dir: Path, thres: Double, parser: FastaParser[A]): KleisliP[Path, Sequence[A]] =
    filterSimilarSequences(dir, thres, parser)

  // FIXME: ugly as shit
  def filterSimilarSequences[A](dir: Path, thres: Double, parser: FastaParser[A]): KleisliP[Path, Sequence[A]] =
    KleisliP { clustalΩ ⇒
      val fastas = for {
        file   ← dir.children("*.{fasta,fas,fna,faa,ffn,frna}")
        parsed ← parser.fromFile(file)
        fasta  ← Process.eval(parsed.toTask(identity))
      } yield (file, fasta)

      val filtered = fastas map { case (path, fasta) ⇒
        val isSimilar = isFirstSimilarToRest(fasta, path, thres).apply(clustalΩ)
        isSimilar.filter(identity).map(_ ⇒ fasta.entries.head)
      }

      merge.mergeN(filtered)
    }

  def isFirstSimilarToRest[A](fasta: Fasta[A], fastaFile: Path, thres: Double): KleisliP[Path, Boolean] =
    Clustal.withDistanceMatrixOf(fastaFile) { matrix ⇒
      similarOverThreshold(fasta.entries.head, thres, matrix) <* matrix.delete
    }

  // TODO: use atto, it will be nicer than raw string manipulation (maybe
  // slower, but who cares?; and also get rid of that nasty "+ 12")
  def similarOverThreshold[A](seq: Sequence[A], thres: Double, matrix: Path): Process[Task, Boolean] = {
    def maxDistanceInLine(line: String): Maybe[Double] = {
      val distances = line.drop(seq.header.length + 12).split("\\s+").toList
      distances.traverse(_.parseDouble.toMaybe).map(_.max)
    }

    val matrixLines = nio.file.linesR(matrix)
    val maxDistance = matrixLines.find(_ startsWith seq.header).map(maxDistanceInLine)
    maxDistance map { _ exists (_ >= thres) }
  }

}
