package funpep

import java.nio.file.Path

import scala.Double.{ NegativeInfinity ⇒ -∞ }

import scalaz._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.std.anyVal._
import scalaz.std.list._
import scalaz.std.string._
import scalaz.syntax.applicative._
import scalaz.syntax.foldable._
import scalaz.syntax.kleisli._
import scalaz.syntax.semigroup._
import scalaz.syntax.std.option._

import contrib._
import data._
import util.functions._
import util.types._
import util.ops.disjunction._
import util.ops.foldable._
import util.ops.path._


final class CSVReporter[A] private (
  val directory: Path,
  val reference: Fasta[A],
  val filtered:  Fasta[A]
)(implicit ev: A ⇒ Compound) {

  import CSVReporter._

  lazy val csvHeader     = """"Comparing ID","Reference ID","Similarity Percentage""""
  lazy val referenceSize = reference.entries.size

  def report(file: Path): KleisliP[Path, Unit] =
    reportLines.mapK[Process[Task, ?], Unit] {
      _.prepend(csvHeader :: Nil).intersperse("\n").pipe(text.utf8Encode).to(nio.file.chunkW(file))
    }

  def reportLines: KleisliP[Path, String] =
    for {
      fasta  ← alignmentFasta.liftKleisli
      matrix ← parseMatrixOf(fasta)
      lines  ← csvLines(matrix).toProcess.liftKleisli
    } yield lines

  def csvLines(matrix: Matrix): IList[String] = {
    import scalaz.syntax.traverse._

    lazy val matLines = matrix.filterWithKey(
      (k, _) ⇒ findSequence(filtered, k).isJust
    ).toList.toIList

    lazy val csvLines = matLines traverse {
      case (h, (i, d)) ⇒ csvLine(i, (h, d), matrix)
    }

    csvLines | IList.empty
  }

  def csvLine(lineIndex: Int, line: MatrixLine, matrix: Matrix): Maybe[String] = {
    lazy val lineHeader = line._1
    lazy val lineDists  = line._2.toIList.updated(lineIndex, -∞).take(referenceSize)

    for {
      maxValue ← lineDists.maximum.toMaybe
      maxIndex ← lineDists.indexOf(maxValue).toMaybe
      compared ← indexedMatrix(matrix).lookup(maxIndex).toMaybe
      cmpSeq   ← findSequence(filtered, lineHeader)
      refSeq   ← findSequence(reference, compared._1)
    } yield s""""${cmpSeq.header}","${refSeq.header}","$maxValue""""
  }

  private def indexedMatrix(matrix: Matrix): Int ==>> (String, NonEmptyList[Double]) =
    ==>>.fromList(matrix.toList map { case (h, (i, d)) ⇒ (i, (h, d)) })

  private def findSequence(fasta: Fasta[A], header: String): Maybe[Sequence[A]] =
    fasta.entries.toIList.find(_.header startsWith header).toMaybe

  private def alignmentFasta: Process[Task, Path] = {
    val path = directory / "csv-distmat.fasta"
    (reference ⊹ filtered).toFile(path) >| path
  }

  private def parseMatrixOf(fasta: Path): KleisliP[Path, Matrix] =
    Clustal.withDistanceMatrixOf(fasta) { matrix ⇒
      MatrixParser.fromFileW(matrix) <* matrix.delete <* fasta.delete
    }

}

object CSVReporter {

  type Matrix     = String ==>> (Int, NonEmptyList[Double])
  type MatrixLine = (String, NonEmptyList[Double])

  object MatrixParser {

    import atto._
    import atto.parser.all._
    import atto.syntax.all._
    import util.parsers._

    lazy val matrix: Parser[Matrix] = matrixHead ~> many1(matrixLine).map(linesToMatrix)

    lazy val matrixHead = takeLine.void
    lazy val matrixLine = header ~ distances

    lazy val header    = takeWhile(_ != ' ') <~ char(' ')
    lazy val distances = sepBy1(double, horizontalWhitespace) <~ (eol | eoi)

    def fromString(str: String): ErrorMsg ∨ Matrix =
      matrix.parseOnly(str).either

    def fromFile(path: Path): Process[Task, ErrorMsg ∨ Matrix] =
      textR(path).map(fromString)

    def fromFileW(path: Path): Process[Task, Matrix] =
      fromFile(path) flatMap {
        parsed ⇒ Process.eval(parsed.toTask(identity))
      }

    private def linesToMatrix(lines: NonEmptyList[MatrixLine]): Matrix =
      (lines.zipWithIndex map { case ((h, d), i) ⇒ (h, (i, d)) }).toMap

  }

  def report[A](reference: Fasta[A], filtered: Fasta[A], csvFile: Path)(implicit ev: A ⇒ Compound): KleisliP[Path, Unit] =
    new CSVReporter(csvFile.parent, reference, filtered).report(csvFile)

  def report[A](reference: Path, filtered: Path, csvFile: Path, parser: FastaParser[A])(implicit ev: A ⇒ Compound): KleisliP[Path, Unit] =
    for {
      ref ← parser.fromFileW(reference).liftKleisli
      fil ← parser.fromFileW(filtered).liftKleisli
      out ← report(ref, fil, csvFile)
    } yield out

}
