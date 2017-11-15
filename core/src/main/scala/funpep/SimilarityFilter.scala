package funpep

import java.nio.file.Path
import java.time.Instant.now

import scalaz._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.stream.merge._
import scalaz.std.list._
import scalaz.syntax.applicative._
import scalaz.syntax.std.string._
import data._
import contrib._
import util.types._
import util.ops.path._


object  SimilarityFilter {

  def apply[A](dir: Path, thres: Double, parser: FastaParser[A])(implicit st: Strategy): KleisliP[Path, Sequence[A]] =
    filterSimilarSequences(dir, thres, parser)

  // FIXME: ugly as shit
  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Any"))
  def filterSimilarSequences[A](dir: Path, thres: Double, parser: FastaParser[A])(implicit st: Strategy): KleisliP[Path, Sequence[A]] =
    KleisliP { clustalΩ ⇒
      val fastas: Process[Task, (Path, Fasta[A])] = for {
        file   ← dir.children("*.{fasta,fas,fna,faa,ffn,frna}")
        fasta  ← parser.fromFileW(file)
      } yield (file, fasta)

      val filtered: Process[Task, Process[Task, Sequence[A]]] = fastas map {
        case (path, fasta) ⇒
          val isSimilar = isFirstSimilarToRest(fasta, path, thres).apply(clustalΩ)
          isSimilar.filter(identity) >| fasta.entries.head
      }

      mergeN(filtered)
    }

  def isFirstSimilarToRest[A](fasta: Fasta[A], fastaFile: Path, thres: Double): KleisliP[Path, Boolean] ={
    Clustal.withDistanceMatrixOf(fastaFile) {
      matrix ⇒
      similarOverThreshold(fasta.entries.head, thres, matrix) <* matrix.delete
    }}

  // TODO: use atto, it will be nicer than raw string manipulation (maybe
  // slower, but who cares?; and also get rid of that nasty "+ 12")
  def similarOverThreshold[A](seq: Sequence[A], thres: Double, matrix: Path): Process[Task, Boolean] = {
    import scalaz.syntax.traverse._

    def maxDistanceInLine(line: String): Maybe[Double] = {
      val distances = line.drop(seq.header.split(" ")(0).length).trim.substring(2).split("\\s+").toList
      distances.traverse(_.parseDouble.toMaybe).map(_.max)
    }

    val matrixLines = nio.file.linesR(matrix)
    val h = seq.header.split(" ")(0)

    val maxDistance = matrixLines.find(_ startsWith h).map(maxDistanceInLine)
    maxDistance map { _ exists (_ >= thres) }
  }

}
