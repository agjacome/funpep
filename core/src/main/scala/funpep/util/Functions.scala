package funpep
package util

import java.nio.file.{ Path, Paths }

import scalaz.concurrent._
import scalaz.stream._


private[util] trait Functions {

  def devnull: Path = Paths.get("/dev/null")

  def discard[A]: A ⇒ Unit = _ ⇒ ()

  def AsyncP[A](a: ⇒ A): Process[Task, A] = Process.eval(Task.delay(a))

}
