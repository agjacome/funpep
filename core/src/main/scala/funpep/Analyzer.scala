package funpep

import java.nio.file.{Path, Paths}
import java.time.Instant.now
import java.nio.file.{Paths, Files}

import scalaz.concurrent.{Strategy, Task}
import scalaz.stream._

import scalaz.syntax.applicative._
import scalaz.syntax.kleisli._
import scalaz.syntax.semigroup._
import scalaz.syntax.nel._
import com.typesafe.scalalogging._
import atto._
import contrib._
import data._
import util.functions._
import util.types._
import util.ops.path._



// TODO: handle failures; logging through Writer of NonEmptyList[String],
// provide a scalaz-stream Sink as argument and just log to it ???
final class Analyzer[A]  private (
  val database: Path,
  val parser:   FastaParser[A]
)(implicit ev: A ⇒ Compound, st: Strategy) {

  import Analysis._
  import Analyzer._



  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Any"))
  def create(ref: Fasta[A], cmp: Fasta[A], thres: Double, annots: Annotations): Process[Task, Analysis] =
    for {
      a ← AsyncP(Analysis(database, thres, annots))
      _ ← a.directory.createDir
      _ ← a.toFile(a.metadata)
      _ ← ref.toFile(a.reference)
      _ ← cmp.toFile(a.comparing)
    } yield a

  def analyze(analysis: Analysis): KleisliP[Path, Analysis] = {
    for {
      ref ← parser.fromFileW(analysis.reference).liftKleisli
      cmp ← parser.fromFileW(analysis.comparing).liftKleisli
      out ← analyze(analysis, ref, cmp)
    } yield out
  }

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Any"))
  def analyze(analysis: Analysis, ref: Fasta[A], cmp: Fasta[A]): KleisliP[Path, Analysis] = {
    val proc = for {
      in ← start(analysis)
      _ ← split(ref, cmp, in.temporal)
      fil ← filter(ref, in)
      _ ← fil.toFile(in.filtered).liftKleisli
      _ ← align(ref, fil, in)
      _ ← report(ref, fil, in)
      out ← finish(in)
    } yield out

    proc.mapK[Process[Task, ?], Analysis] {
      _.onFailure(failed(analysis, _)).onComplete(isReallyCompleted(analysis))
    }
  }

  def failed(analysis: Analysis, err: Throwable): Process[Task, Analysis] = {
    clear(analysis.withStatus(Failed(now, err.toString)))
  }

  def isReallyCompleted(analysis: Analysis): Process[Task, Analysis] = {
    if(!Files.exists(analysis.filtered))
      clear(analysis.withStatus(Failed(now, "Selected threshold value is too high.")))
    else
      analysis.withStatus(Finished(now)).toFile(analysis.metadata) >| analysis
  }

  def clear(analysis: Analysis): Process[Task, Analysis] =
    analysis.temporal.deleteRecursive  *>
    analysis.filtered.delete           *>
    analysis.alignment.delete          *>
    analysis.newick.delete             *>
    analysis.phyloXML.delete           *>
    analysis.treeImage.delete          *>
    analysis.csvReport.delete          *>
    analysis.mergedSeq.delete          *>
    analysis.toFile(analysis.metadata) >| analysis

  private def start(analysis: Analysis): KleisliP[Path, Analysis] = {
    val started = analysis.withStatus(Started(now))
    (started.toFile(started.metadata) *> started.temporal.createDir >| started).liftKleisli
  }

  private def finish(analysis: Analysis): KleisliP[Path, Analysis] = {
    val finished = analysis.withStatus(Finished(now))
    (finished.toFile(finished.metadata) *>
     finished.temporal.deleteRecursive  *>
     finished.mergedSeq.delete          >| finished).liftKleisli
  }

  private def fail(analysis: Analysis, error: String): KleisliP[Path, Analysis] = {
    val failed = analysis.withStatus(Failed(now, error))
    (failed.toFile(failed.metadata) *> failed.temporal.deleteRecursive >| failed).liftKleisli
  }

  private def split(reference: Fasta[A], comparing: Fasta[A], temporal: Path): KleisliP[Path, Unit] ={
    MergeSplitter(comparing, reference, temporal).reduceMap(_.wrapNel).void.liftKleisli
  }

  private def filter(reference: Fasta[A], analysis: Analysis): KleisliP[Path, Fasta[A]] = {
    val filterProc = SimilarityFilter(analysis.temporal, analysis.threshold, parser)
    filterProc.mapK[Process[Task, ?], Fasta[A]](_.reduceMap(seq ⇒ Fasta(seq)))
  }

  private def align(reference: Fasta[A], filtered: Fasta[A], analysis: Analysis): KleisliP[Path, Unit] = {
    for {
      fasta  ← treeFasta(reference, filtered, analysis).liftKleisli
      guide  ←  Clustal.guideTree(fasta, analysis.alignment, analysis.newick)
    } yield guide
  }

  private def treeFasta(reference: Fasta[A], filtered: Fasta[A], analysis: Analysis): Process[Task, Path] = {
    (reference ⊹ filtered).toFile(analysis.mergedSeq) >| analysis.mergedSeq
  }

  private def report(reference: Fasta[A], filtered: Fasta[A], analysis: Analysis): KleisliP[Path, Unit] = {
    CSVReporter.report(reference, filtered, analysis.csvReport, analysis.threshold) *>
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
    def mergedSeq: Path = analysis.directory / "merged.fasta"
  }

  def apply[A](database: Path)(implicit parser: Parser[A], ev: A ⇒ Compound, st: Strategy): Analyzer[A] =
    new Analyzer(database, FastaParser[A])

}
object LogInside extends LazyLogging{
  def log(x: String): Unit = {
    val logger = Logger("name")
    logger.info(x+"")
  }
}
