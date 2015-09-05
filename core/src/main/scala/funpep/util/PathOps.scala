package funpep
package util

import java.nio.file.{ Path, Paths }
import java.nio.channels.AsynchronousFileChannel

final class PathOps private[util] (val self: Path) extends AnyVal {

  def openAsyncChannel: AsynchronousFileChannel =
    AsynchronousFileChannel.open(self)

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
