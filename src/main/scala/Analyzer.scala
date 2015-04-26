package es.uvigo.ei.sing.funpep

import scalaz._
import scalaz.Scalaz._
import scalaz.effect.IO

import contrib.Clustal
import data.{ Analysis, Config, Fasta }
import data.Analysis._
import util.IOUtils._


// TODO: !!! temporal stub implementation, needs to be refined.
// Things to consider:
//   * handle failure cases: change state to Failed, persist error message?
//   * analysis should not be kept in memory all times, it's already persisted
//     in settingsPath, it can be read when required
//   * all methods block execution, wrap into Futures?
//   * create an actual analysis job queue and enqueue them upon calling
//     "enqueue"
//   * receive an onStateChange function to be called on any Analysis.status
//     change (maybe an Analysis ⇒ Unit??), can StateT be used instead?
//   * split "Started" state into different analysis stages (splitted, filtered,
//     aligned, ...)
//   * delete temporalPath and its contents when Finished or Failed state
//     reached
final class Analyzer private (val analysis: Analysis, val config: Config) {

  val persistentPath = (config.databasePath / analysis.uuid.toString)
  val temporalPath   = (config.temporalPath / analysis.uuid.toString)

  lazy val settingsPath  = persistentPath / "settings.json"
  lazy val comparingPath = persistentPath / "comparing.fasta"
  lazy val referencePath = persistentPath / "reference.fasta"

  lazy val filteredPath  = persistentPath / "filtered.fasta"
  lazy val alignmentPath = persistentPath / "alignment.fasta"
  lazy val guidetreePath = persistentPath / "tree.newick"

  def analyze(comparing: Fasta, reference: Fasta): ⇄[Analyzer] =
    for {
      created  ← create()
      enqueued ← created.enqueue(comparing, reference)
      splitted ← enqueued.split()
      filtered ← splitted.filter()
      finished ← filtered.align()
    } yield finished

  // FIXME: !!!
  def create(): ⇄[Analyzer] =
    for {
      _ ← persistentPath.createDir
      _ ← temporalPath.createDir
    } yield changeStateTo(Created)

  // TODO: create analysis queue and actually enqueue jobs
  def enqueue(comparing: Fasta, reference: Fasta): ⇄[Analyzer] =
    analysis.status match {
      case Created ⇒ persistAnalysis(comparing, reference)
      case Failed  ⇒ IO(this).catchLeft
      case _       ⇒ IO(invalidState("Analysis already enqueued"))
    }

  def split(): ⇄[Analyzer] =
    analysis.status match {
      case Queued ⇒ persistSplits()
      case Failed ⇒ IO(this).catchLeft
      case _      ⇒ IO(invalidState("Analysis' fastas already splitted"))
    }

  def filter(): ⇄[Analyzer] =
    analysis.status match {
      case Started ⇒ persistFilter()
      case Failed  ⇒ IO(this).catchLeft
      case _       ⇒ IO(invalidState("Analysis' fastas already filtered"))
    }

  def align(): ⇄[Analyzer] =
    analysis.status match {
      case Started ⇒ persistAlignment()
      case Failed  ⇒ IO(this).catchLeft
      case _       ⇒ IO(invalidState("Analysis' filtered fasta already aligned"))
    }


  private def changeStateTo(status: Status): Analyzer =
    new Analyzer(analysis.copy(status = status), config)

  private def invalidState(cause: String): Throwable ∨ Analyzer =
    new IllegalStateException(cause).left[Analyzer]


  // FIXME: no need for monads (for-expressions) in the following methods,
  // applicative suffices in most cases

  private def persistAnalysis(comparing: Fasta, reference: Fasta): ⇄[Analyzer] =
    for {
      _ ← analysis.toJsonFile(settingsPath)
      _ ← comparing.toFile(comparingPath)
      _ ← reference.toFile(referencePath)
    } yield changeStateTo(Queued)

  private def persistSplits(): ⇄[Analyzer] =
    for {
      comparing ← Fasta(comparingPath)
      reference ← Fasta(referencePath)
      _         ← Splitter.splitAndSaveTo(temporalPath)(comparing, reference)
    } yield changeStateTo(Started)

  private def persistFilter(): ⇄[Analyzer] =
    for {
      filtered  ← Filter.filterSimilarEntries(temporalPath, analysis.threshold)(config)
      reference ← Fasta(referencePath)
      _         ← Fasta(filtered <::: reference.entries).toFile(filteredPath) // FIXME
    } yield this

  private def persistAlignment(): ⇄[Analyzer] =
    for {
      _ ← Clustal.guideTree(filteredPath, alignmentPath, guidetreePath)(config)
    } yield changeStateTo(Finished)

}

object Analyzer {

  import Config.syntax._

  def apply(analysis: Analysis): Configured[Analyzer] =
    Configured(config ⇒ new Analyzer(analysis, config))

}
