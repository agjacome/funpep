package funpep
package util

import scalaz._

final class StringOps private[util] (val self: String) extends AnyVal {

  def words:   IList[String] = IList { self.split("\\s+"): _* }
  def toIList: IList[Char]   = IList { self: _* }

}

trait ToStringOps {
  implicit def ToStringOps(self: String): StringOps = new StringOps(self)
}
