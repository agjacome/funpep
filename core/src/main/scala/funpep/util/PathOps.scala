package funpep
package util

import java.nio.file.Path
import java.nio.channels.AsynchronousFileChannel

final class PathOps private[util] (val self: Path) extends AnyVal {

  def openAsyncChannel: AsynchronousFileChannel =
    AsynchronousFileChannel.open(self)

  def absolute: Path = self.toAbsolutePath

  def /(child: Path):   Path = self.resolve(child)
  def /(child: String): Path = self.resolve(child)

  def |(sibling: Path):   Path = self.resolveSibling(sibling)
  def |(sibling: String): Path = self.resolveSibling(sibling)

}

trait ToPathOps {
  implicit def ToPathOps(self: Path): PathOps = new PathOps(self)
}
