package funpep
package util

import java.nio.file.{ Files, Path, Paths }

import scala.collection.JavaConverters._

import scalaz._
import scalaz.std.list._
import scalaz.syntax.foldable._

import scalaz.concurrent._
import scalaz.stream._

import util.ops.foldable._


private[util] trait Functions {

  def devnull: Path = Paths.get("/dev/null")

  def discard[A]: A ⇒ Unit = _ ⇒ ()

  def AsyncP[A](a: ⇒ A): Process[Task, A] =
    Process.eval(Task.delay(a))

  def MergeN[A](ps: ⇒ Process[Task, Process[Task, A]]): Process[Task, A] =
    merge.mergeN(ps)

  def linesR(path: Path): Process[Task, IList[String]] =
    AsyncP { Files.readAllLines(path).asScala.toList.toIList }

  def textR(path: Path): Process[Task, String] =
    linesR(path).map(_.mkString(identity, "\n"))

}
