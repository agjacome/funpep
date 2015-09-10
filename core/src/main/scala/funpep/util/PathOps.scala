package funpep
package util

import java.nio.file.{ DirectoryStream, Files, Path, Paths }
import java.nio.channels.AsynchronousFileChannel

import scalaz.concurrent._
import scalaz.stream._
import scalaz.syntax.functor._

import functions.AsyncP


final class PathOps private[util] (val self: Path) extends AnyVal {

  def openAsyncChannel: AsynchronousFileChannel =
    AsynchronousFileChannel.open(self)

  def exists: Process[Task, Boolean] =
    AsyncP(Files.exists(self))

  def create: Process[Task, Unit] =
    AsyncP(Files.createFile(self)).void

  def delete: Process[Task, Unit] =
    AsyncP(Files.deleteIfExists(self)).void

  def children(glob: String): Process[Task, Path] = {
    import Cause.{ Terminated, End }

    io.resource(Task.delay(Files.newDirectoryStream(self, glob)))(s ⇒ Task.delay(s.close)) { s ⇒
      val iter = s.iterator
      Task.delay {
        if (iter.hasNext) iter.next
        else throw Terminated(End)
      }
    }
  }

  def absolute: Path = self.toAbsolutePath

  def /(child: Path):   Path = self.resolve(child)
  def /(child: String): Path = self.resolve(child)

  def |(sibling: Path):   Path = self.resolveSibling(sibling)
  def |(sibling: String): Path = self.resolveSibling(sibling)

  def +(extension: String): Path = Paths.get(self.toString + extension)

}

trait ToPathOps {
  implicit def ToFunpepPathOps(self: Path): PathOps = new PathOps(self)
}
