package es.uvigo.ei.sing.funpep

import java.nio.file.Path
import java.util.UUID

import scalaz.Scalaz._
import scalaz.effect.IO

import data._
import contrib.Clustal
import util.IOUtils._


// TODO: Handle failure cases (set status = Job.Failed)
// TODO: Log info of every action. Use log to display errors instead of
// sys.error and printStackTrace
final class Analyzer private (val config: Config) {

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Var"))
  @volatile private var running: Boolean = false

  private val loop = new Thread(new Runnable {
    override def run: Unit =
      while (running) {
        val id = AnalyzerQueue.dequeue
        process(id).run.unsafePerformIO valueOr { error ⇒
          error.printStackTrace()
          sys.error(s"Unexpected error while analyzing job $id")
        }
      }
  })

  def start(): Unit = { running = true;  loop.start() }
  def stop():  Unit = { running = false; loop.join()  }

  def analyze(job: Job, comparing: Fasta, reference: Fasta): IOThrowable[Unit] =
    create(job, comparing, reference)             *>
    IO(AnalyzerQueue.enqueue(job.id)).catchLeft *>
    updateStatus(job.id, Job.Queued)

  def analysis (id: UUID): Path = database(id) / "analysis.json"
  def comparing(id: UUID): Path = database(id) / "comparing.fasta"
  def reference(id: UUID): Path = database(id) / "reference.fasta"
  def filtered (id: UUID): Path = database(id) / "filtered.fasta"
  def alignment(id: UUID): Path = database(id) / "alignment.fasta"
  def guidetree(id: UUID): Path = database(id) / "tree.newick"

  def status(id: UUID): IOThrowable[Job.Status] = Job(analysis(id)).map(_.status)

  private def database(id: UUID): Path = config.databasePath / id.toString
  private def temporal(id: UUID): Path = config.temporalPath / id.toString

  private def process(id: UUID): IOThrowable[Unit] =
    updateStatus(id, Job.Started) *>
    split(id)                     *>
    filter(id)                    *>
    align(id)                     *>
    temporal(id).deleteDir        *>
    updateStatus(id, Job.Finished)

  private def updateStatus(id: UUID, status: Job.Status): IOThrowable[Unit] =
    Job(analysis(id)).map(_.copy(status = status)) >>= { _.toJsonFile(analysis(id)) }

  private def split(id: UUID): IOThrowable[Unit] =
    for {
      cmp ← Fasta(comparing(id))
      ref ← Fasta(reference(id))
      fin ← Splitter.splitAndSaveTo(temporal(id))(cmp, ref)
    } yield fin

  private def filter(id: UUID): IOThrowable[Unit] =
    for {
      job ← Job(analysis(id))
      sim ← Filter.filterSimilarEntries(temporal(id), job.threshold)(config)
      ref ← Fasta(reference(id))
      fin ← Fasta(sim <::: ref.entries).toFile(filtered(id))
    } yield fin

  private def align(id: UUID): IOThrowable[Unit] =
    Clustal.guideTree(filtered(id), alignment(id), guidetree(id))(config) map { _ ⇒ () }

  private def create(job: Job, comparing: Fasta, reference: Fasta): IOThrowable[Unit] =
    database(job.id).createDir                                  *>
    temporal(job.id).createDir                                  *>
    job.copy(status = Job.Created).toJsonFile(analysis(job.id)) *>
    comparing.toFile(this.comparing(job.id))                    *>
    reference.toFile(this.reference(job.id))

  private object AnalyzerQueue {

    import java.util.concurrent.{ LinkedBlockingQueue ⇒ Queue }
    import scala.collection.JavaConverters._

    // FIXME: mutable queue & unsafePerformIO, can we do better?
    private val queue: Queue[UUID] = {
      val io    = config.jobQueuePath.contentsAsList
      val lines = io.run.unsafePerformIO valueOr { error ⇒
        error.printStackTrace()
        sys.error("Could not correctly read Job queue")
      }

      new Queue(lines.filterNot(_.isEmpty).map(UUID.fromString).asJava)
    }

    def enqueue(id: UUID): Unit = {
      queue.put(id)
      updateQueueFile()
    }

    def dequeue: UUID = {
      val id = queue.take()
      updateQueueFile()
      id
    }

    // FIXME: do not unsafePerformIO here, return IOThrowable[Unit] to upper
    // level. enqueue/dequeue should also return that instead of Unit.
    private def updateQueueFile(): Unit = {
      val lines = queue.asScala mkString nl
      val write = config.jobQueuePath.openIOWriter.bracket(_.closeIO) {
        _.writeIO(lines).catchLeft
      }

      write.run.unsafePerformIO valueOr { error ⇒
        error.printStackTrace()
        sys.error(s"Could not correctly write Job queue${nl}Current queue:${nl}${lines}")
      }
    }

  }

}

object Analyzer {

  import Config.syntax._

  def apply(config: Config): Analyzer =
    new Analyzer(config)

  def apply: Configured[Analyzer] =
    Configured { new Analyzer(_) }

}