package funpep.server

import java.nio.file.Path
import java.nio.file.StandardOpenOption._
import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue

import scala.collection.JavaConverters._

import scalaz._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.std.list._
import scalaz.syntax.applicative._
import scalaz.syntax.foldable._
import scalaz.syntax.kleisli._
import scalaz.syntax.std.option._

import funpep.Analyzer
import funpep.data._
import funpep.util.functions._
import funpep.util.types._
import funpep.util.ops.foldable._
import funpep.util.ops.path._


// FIXME: replace with a less effecful and safer design
final class AnalyzerQueue[A] private (val analyzer: Analyzer[A], val queue: Path) {

  import Analysis.Annotations

  private val q = new ArrayBlockingQueue[UUID](5000)

  def analyzerLoop: KleisliP[Path, Analysis]  =
    pop.repeat.liftKleisli.flatMap(analyzer.analyze)

  def count: Int =
    q.size

  def position(uuid: UUID): Maybe[Int] =
    q.asScala.toList.toIList.indexOf(uuid)(Equal.equalA).toMaybe

  def push(
    reference:   Fasta[A],
    comparing:   Fasta[A],
    threshold:   Double,
    annotations: Annotations
  ): Process[Task, Analysis] =
    for {
      a ← analyzer.create(reference, comparing, threshold, annotations)
      _ ← enqueue(a.id)
    } yield a

  def pop: Process[Task, Analysis] =
    dequeue flatMap {
      id ⇒ AnalysisParser.fromFileW(analyzer.database / id.toString / "analysis.metadata")
    }

  private def enqueue(id: UUID): Process[Task, Unit] =
    AsyncP(q.put(id)) <* write

  private def dequeue: Process[Task, UUID] =
    AsyncP(q.take()) <* write

  private def write: Process[Task, Unit] = {
    val lines = q.asScala.toList.map(_.toString)

    lines.toProcess.intersperse("\n").pipe(text.utf8Encode).to(
      nio.file.chunkW(queue.openAsyncChannel(WRITE, CREATE, TRUNCATE_EXISTING))
    )
  }

}

object AnalyzerQueue {

  def apply[A](analyzer: Analyzer[A], queue: Path): AnalyzerQueue[A] =
    new AnalyzerQueue(analyzer, queue)

  def apply[A](analyzer: Analyzer[A]): AnalyzerQueue[A] =
    apply(analyzer, analyzer.database / "pending-analysis.queue")

}
