package funpep
package util

import java.nio.file.{ Files, Path, Paths }
import java.util.concurrent.{ ExecutorService, Executors }

import scala.collection.JavaConverters._

import scalaz._
import scalaz.std.list._
import scalaz.syntax.foldable._

import scalaz.concurrent._
import scalaz.stream._

import util.ops.foldable._


private[util] trait Functions {

  import Strategy._

  lazy val processors: Int  = Runtime.getRuntime.availableProcessors
  lazy val devnull:    Path = Paths.get("/dev/null")

  def AsyncP[A](a: ⇒ A): Process[Task, A] =
    Process.eval { Task(a) }

  def linesR(path: Path): Process[Task, IList[String]] =
    Process.eval(Task.delay {
      Files.readAllLines(path).asScala.toList.toIList
    })

  def textR(path: Path): Process[Task, String] =
    linesR(path).map(_.mkString(identity, "\n"))

  def fixedPoolExecutorService(n: Int): ExecutorService =
    Executors.newFixedThreadPool(n, DefaultDaemonThreadFactory)

  def fixedPoolStrategy(n: Int): Strategy =
    Executor(fixedPoolExecutorService(n))

}
