package funpep
package util

import java.nio.file.{ Path, Paths }

import scalaz.IList


final class StringOps private[util] (val self: String) extends AnyVal {

  def words: IList[String] = IList(self.split("\\s+"): _*)

  def toIList: IList[Char] = IList(self: _*)

  def toPath: Path = Paths.get(self)

}

trait ToStringOps {
  implicit def ToStringOps(self: String): StringOps = new StringOps(self)
}