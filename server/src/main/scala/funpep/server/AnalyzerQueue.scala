package funpep.server

import java.nio.file.Path
import java.nio.file.StandardOpenOption._
import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue

import scala.collection.JavaConverters._

import scalaz.concurrent._
import scalaz.stream._
import scalaz.std.list._

import funpep.Analyzer
import funpep.data._
import funpep.util.functions._
import funpep.util.ops.foldable._
import funpep.util.ops.path._


// FIXME: replace with a less effecful and safer design
final class AnalyzerQueue[A] private (val analyzer: Analyzer[A], val queue: Path) {

  import Analysis.Annotations

  private val q = new ArrayBlockingQueue[UUID](5000)

  def count: Int = q.size

  def push(
    reference:   Fasta[A],
    comparing:   Fasta[A],
    threshold:   Double,
    annotations: Annotations
  ): Process[Task, Analysis] =
    for {
      a ← analyzer.create(reference, comparing, threshold, annotations)
      _ ← appendToQueueFile(a.id)
      _ ← AsyncP { q.put(a.id) }
    } yield a

  def pop: Process[Task, Analysis] =
    for {
      id ← AsyncP { q.take() }
      _  ← dropFromQueueFile
      an ← AnalysisParser.fromFileW(analyzer.database / id.toString / "analysis.metadata")
    } yield an

  def write: Process[Task, Unit] = {
    val lines = q.asScala.toList.toProcess.map(_.toString).intersperse("\n")

    lines.pipe(text.utf8Encode).to(
      nio.file.chunkW(queue.openAsyncChannel(WRITE, CREATE, TRUNCATE_EXISTING))
    )
  }

  private def appendToQueueFile(id: UUID): Process[Task, Unit] =
    Process(id.toString).pipe(text.utf8Encode).to(
      nio.file.chunkW(queue.openAsyncChannel(WRITE, APPEND))
    )

  private def dropFromQueueFile: Process[Task, Unit] = {
    val tmp = queue + ".tmp"

    val read = nio.file.linesR(queue).drop(1).intersperse("\n").pipe(text.utf8Encode).to(
      nio.file.chunkW(tmp.openAsyncChannel(WRITE, CREATE, TRUNCATE_EXISTING))
    )

    val write = textR(tmp).pipe(text.utf8Encode).to(
      nio.file.chunkW(queue.openAsyncChannel(WRITE, TRUNCATE_EXISTING))
    )

    // flatMap does not work as expected here, onComplete is required
    read.onComplete(write.onComplete(tmp.delete)) 
  }

}

object AnalyzerQueue {

  def apply[A](analyzer: Analyzer[A], queue: Path): AnalyzerQueue[A] =
    new AnalyzerQueue(analyzer, queue)

  def apply[A](analyzer: Analyzer[A]): AnalyzerQueue[A] =
    apply(analyzer, analyzer.database / "pending-analysis.queue")

}
