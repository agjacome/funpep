package funpep
package util

import java.nio.file._
import java.nio.channels.AsynchronousFileChannel

import org.apache.commons.io.FileUtils

import scalaz.concurrent._
import scalaz.stream._
import scalaz.syntax.applicative._

import functions.AsyncP


final class PathOps private[util] (val self: Path) extends AnyVal {

  def openAsyncChannel: AsynchronousFileChannel =
    AsynchronousFileChannel.open(self)

  def openAsyncChannel(ops: OpenOption*): AsynchronousFileChannel =
    AsynchronousFileChannel.open(self, ops: _*)

  def exists: Process[Task, Boolean] =
    AsyncP(Files.exists(self))

  def create: Process[Task, Unit] =
    AsyncP(Files.createFile(self)).void

  def createIfNotExists: Process[Task, Unit] =
    AsyncP(if (!Files.exists(self)) Files.createFile(self)).void

  def createDir: Process[Task, Unit] =
    AsyncP(Files.createDirectories(self)).void

  def delete: Process[Task, Unit] =
    AsyncP(Files.deleteIfExists(self)).void

  def deleteRecursive: Process[Task, Unit] =
    AsyncP(FileUtils.deleteDirectory(self.toFile))

  def children(glob: String): Process[Task, Path] = {
    import Cause.{ Terminated, End }

    io.resource(Task(Files.newDirectoryStream(self, glob)))(s ⇒ Task(s.close)) { s ⇒
      val iter = s.iterator
      Task {
        if (iter.hasNext) iter.next
        else throw Terminated(End)
      }
    }
  }

  def absolute: Path = self.toAbsolutePath

  def parent: Path = self.getParent

  def /(child: Path):   Path = self.resolve(child)
  def /(child: String): Path = self.resolve(child)

  def |(sibling: Path):   Path = self.resolveSibling(sibling)
  def |(sibling: String): Path = self.resolveSibling(sibling)

  def +(extension: String): Path = Paths.get(self.toString + extension)

}

trait ToPathOps {
  implicit def ToFunpepPathOps(self: Path): PathOps = new PathOps(self)
}
