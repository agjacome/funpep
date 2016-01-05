package funpep

import java.nio.file.Path
import java.time.Instant.now

import scalaz.concurrent.{ Strategy, Task }
import scalaz.stream._
import scalaz.syntax.applicative._
import scalaz.syntax.kleisli._
import scalaz.syntax.nel._

import atto._

import contrib._
import data._
import util.functions._
import util.types._
import util.ops.path._


// TODO: handle failures; logging through Writer of NonEmptyList[String],
// provide a scalaz-stream Sink as argument and just log to it ???
final class Analyzer[A] private (
  val database: Path,
  val parser:   FastaParser[A]
)(implicit ev: A ⇒ Compound, st: Strategy) {

  import Analysis._
  import Analyzer._

  def create(ref: Fasta[A], cmp: Fasta[A], thres: Double, annots: Annotations): Process[Task, Analysis] =
    for {
      a ← AsyncP(Analysis(database, thres, annots))
      _ ← a.directory.createDir
      _ ← a.toFile(a.metadata)
      _ ← ref.toFile(a.reference)
      _ ← cmp.toFile(a.comparing)
    } yield a

  def clear(analysis: Analysis): Process[Task, Analysis] = {
    val clean = analysis.withStatus(Created(now))

    clean.temporal.deleteRecursive  *>
    clean.filtered.delete           *>
    clean.alignment.delete          *>
    clean.newick.delete             *>
    clean.phyloXML.delete           *>
    clean.treeImage.delete          *>
    clean.csvReport.delete          *>
    clean.toFile(analysis.metadata) >| clean
  }

  def analyze(analysis: Analysis): KleisliP[Path, Analysis] =
    for {
      ref ← parser.fromFileW(analysis.reference).liftKleisli
      cmp ← parser.fromFileW(analysis.comparing).liftKleisli
      out ← analyze(analysis, ref, cmp)
    } yield out

  def analyze(analysis: Analysis, ref: Fasta[A], cmp: Fasta[A]): KleisliP[Path, Analysis] =
    for {
      in  ← start(analysis)
      _   ← split(ref, cmp, in.temporal)
      fil ← filter(ref, in)
      _   ← fil.toFile(in.filtered).liftKleisli
      _   ← align(in)
      _   ← report(ref, fil, in)
      out ← finish(in)
    } yield out

  private def start(analysis: Analysis): KleisliP[Path, Analysis] = {
    val started = analysis.withStatus(Started(now))
    (started.toFile(started.metadata) *> started.temporal.createDir >| started).liftKleisli
  }

  private def finish(analysis: Analysis): KleisliP[Path, Analysis] = {
    val finished = analysis.withStatus(Finished(now))
    (finished.toFile(finished.metadata) *> finished.temporal.deleteRecursive >| finished).liftKleisli
  }

  private def fail(analysis: Analysis, error: String): KleisliP[Path, Analysis] = {
    val failed = analysis.withStatus(Failed(now, error))
    (failed.toFile(failed.metadata) *> failed.temporal.deleteRecursive >| failed).liftKleisli
  }

  def split(reference: Fasta[A], comparing: Fasta[A], temporal: Path): KleisliP[Path, Unit] =
    MergeSplitter(comparing, reference, temporal).reduceMap(_.wrapNel).void.liftKleisli

  private def filter(reference: Fasta[A], analysis: Analysis): KleisliP[Path, Fasta[A]] = {
    val filterProc = SimilarityFilter(analysis.temporal, analysis.threshold, parser)
    filterProc.mapK[({ type λ[α] = Process[Task, α] })#λ, Fasta[A]](_.reduceMap(seq ⇒ Fasta(seq)))
  }

  private def align(analysis: Analysis): KleisliP[Path, Unit] =
    Clustal.guideTree(analysis.filtered, analysis.alignment, analysis.newick)

  private def report(reference: Fasta[A], filtered: Fasta[A], analysis: Analysis): KleisliP[Path, Unit] = {
    CSVReporter.report(reference, filtered, analysis.csvReport) *>
    TreeReporter.generateTreeFiles(analysis.newick, analysis.annotations)(analysis.phyloXML, analysis.treeImage).liftKleisli
  }

}

object Analyzer {

  implicit final class AnalysisOps(val analysis: Analysis) extends AnyVal {
    def temporal:  Path = analysis.directory / "tmp"
    def metadata:  Path = analysis.directory / "analysis.data"
    def reference: Path = analysis.directory / "reference.fasta"
    def comparing: Path = analysis.directory / "comparing.fasta"
    def filtered:  Path = analysis.directory / "filtered.fasta"
    def alignment: Path = analysis.directory / "alignment.fasta"
    def newick:    Path = analysis.directory / "similar.newick"
    def phyloXML:  Path = analysis.directory / "similar.phylo.xml"
    def treeImage: Path = analysis.directory / "similar.png"
    def csvReport: Path = analysis.directory / "report.csv"
  }

  def apply[A](database: Path)(implicit parser: Parser[A], ev: A ⇒ Compound, st: Strategy): Analyzer[A] =
    new Analyzer(database, FastaParser[A])

}
