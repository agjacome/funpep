// package es.uvigo.ei.sing.funpep

// import java.io.IOException
// import java.nio.file.Path
// import java.util.UUID

// import scalaz.Scalaz._
// import scalaz.effect.IO

// import com.typesafe.scalalogging.LazyLogging

// import data._
// import data.Config._
// import contrib.Clustal
// import util.IOUtils._


// // TODO: Handle failure cases (set status = Job.Failed). Check existence of
// // files before trying to read/write them.
// final class Analyzer (val config: Config) extends LazyLogging {

  // @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Var"))
  // @volatile private var running: Boolean = false

  // private val loop = new Thread(new Runnable {
    // override def run: Unit =
      // while (running) {
        // AnalyzerQueue.dequeue foreach { id ⇒
          // process(id).run.unsafePerformIO() valueOr { error ⇒
            // logger.error(s"Unexpected error while analyzing job $id", error)
            // sys.exit(-1)
          // }
        // }
      // }
  // })

  // def isRunning: Boolean = running

  // def start(): Unit = this.synchronized { if (!isRunning) { running = true;  loop.start() } }
  // def stop():  Unit = this.synchronized { if (isRunning)  { running = false; loop.join()  } }

  // def analyze(job: Job, comparing: Fasta, reference: Fasta): IOThrowable[Unit] =
    // create(job, comparing, reference)           *>
    // IO(AnalyzerQueue.enqueue(job.id)).catchLeft *>
    // updateStatus(job.id, Job.Queued)

  // def queueSize: Int = AnalyzerQueue.queue.size()

  // def analysis (id: UUID): Path = database(id) / "analysis.json"
  // def comparing(id: UUID): Path = database(id) / "comparing.fasta"
  // def reference(id: UUID): Path = database(id) / "reference.fasta"
  // def filtered (id: UUID): Path = database(id) / "filtered.fasta"
  // def alignment(id: UUID): Path = database(id) / "alignment.fasta"
  // def guidetree(id: UUID): Path = database(id) / "tree.newick"
  // def csvReport(id: UUID): Path = database(id) / "report.csv"

  // def status(id: UUID): IOThrowable[Job.Status] =
    // analysis(id).exists >>= { ∃ ⇒
      // if (∃) Job(analysis(id)).map(_.status)
      // else   IO(new IOException(s"Job with id '$id' does not exist").left[Job.Status])
    // }

  // private def database(id: UUID): Path = config.database / id.toString
  // private def temporal(id: UUID): Path = database(id) / "tmp"

  // @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.NoNeedForMonad"))
  // private def process(id: UUID): IOThrowable[Unit] = {
    // logger.info(s"Processing job $id")
    // for {
     // _ ← updateStatus(id, Job.Started)
     // _ ← split(id)
     // _ ← filter(id)
     // _ ← align(id)
     // _ ← report(id)
     // _ ← temporal(id).deleteDir
     // _ ← updateStatus(id, Job.Finished)
    // } yield logger.info(s"Analysis of job $id finished")
  // }

  // private def updateStatus(id: UUID, status: Job.Status): IOThrowable[Unit] =
    // Job(analysis(id)).map(_.copy(status = status)) >>= { _.toJsonFile(analysis(id)) }

  // private def split(id: UUID): IOThrowable[Unit] =
    // for {
      // cmp ← Fasta(comparing(id))
      // ref ← Fasta(reference(id))
      // fin ← Splitter.splitAndSaveTo(temporal(id))(cmp, ref)
    // } yield fin

  // private def filter(id: UUID): IOThrowable[Unit] =
    // for {
      // job ← Job(analysis(id))
      // sim ← Filter.parFilterSimilarEntries(temporal(id), job.threshold)(config)
      // ref ← Fasta(reference(id))
      // fin ← Fasta(sim <::: ref.entries).toFile(filtered(id))
    // } yield fin

  // private def align(id: UUID): IOThrowable[Unit] =
    // Clustal.guideTree(filtered(id), alignment(id), guidetree(id))(config) map { _ ⇒ () }

  // private def report(id: UUID): IOThrowable[Unit] =
    // for {
      // csv   ← Reporter.generateReport(reference(id), filtered(id))(config)
      // write ← csvReport(id).openIOWriter.bracket(_.closeIO)(_.writeIO(csv)).catchLeft
    // } yield write

  // private def create(job: Job, comparing: Fasta, reference: Fasta): IOThrowable[Unit] = {
    // logger.info(s"Creating files for job ${job.id}")
    // database(job.id).createDir                                  *>
    // temporal(job.id).createDir                                  *>
    // job.copy(status = Job.Created).toJsonFile(analysis(job.id)) *>
    // comparing.toFile(this.comparing(job.id))                    *>
    // reference.toFile(this.reference(job.id))
  // }

  // private object AnalyzerQueue {

    // import java.util.concurrent.{ LinkedBlockingQueue ⇒ Queue, TimeUnit }
    // import scala.collection.JavaConverters._

    // // FIXME: mutable queue & unsafePerformIO, can we do better?
    // val queue: Queue[UUID] = {
      // val io    = config.jobQueue.contentsAsList
      // val lines = io.run.unsafePerformIO() valueOr { error ⇒
        // logger.error("Could not correctly read Job queue", error)
        // sys.exit(-1)
      // }

      // new Queue(lines.filterNot(_.isEmpty).map(UUID.fromString).asJava)
    // }

    // def enqueue(id: UUID): Unit = {
      // queue.put(id)
      // updateQueueFile()
    // }

    // def dequeue: Option[UUID] = {
      // val id = Option(queue.poll(3L, TimeUnit.SECONDS))
      // id.foreach(_ ⇒ updateQueueFile())
      // id
    // }

    // // FIXME: do not unsafePerformIO here, return IOThrowable[Unit] to upper
    // // level. enqueue/dequeue should also return that instead of Unit.
    // private def updateQueueFile(): Unit = {
      // val lines = queue.asScala mkString nl
      // val write = config.jobQueue.openIOWriter.bracket(_.closeIO) {
        // _.writeIO(lines).catchLeft
      // }

      // write.run.unsafePerformIO() valueOr { error ⇒
        // logger.error("Could not correctly write Job queue", error)
        // logger.error(s"Job queue before fail:${nl}${lines}")
        // sys.exit(-1)
      // }
    // }

  // }

// }

// object Analyzer {

  // def apply(config: Config): Analyzer =
    // new Analyzer(config)

  // def apply: Configured[Analyzer] =
    // Configured { new Analyzer(_) }

// }