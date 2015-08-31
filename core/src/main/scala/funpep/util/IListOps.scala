package funpep
package util

import scalaz.{ IList, Maybe }
import scalaz.syntax.std.option._


final class IListOps[A] private[util] (val self: IList[A]) extends AnyVal {

  def tailMaybe: Maybe[IList[A]] = self.tailOption.toMaybe

  def grouped(n: Int): IList[IList[A]] = {
    def groups = math.ceil(self.length / n.toDouble).toInt
    Stream.from(0, n).take(groups).foldLeft(IList.empty[IList[A]]) {
      (chunks, index) ⇒ self.slice(index, index + n) :: chunks
    }
  }

}

trait ToIListOps {
  implicit def ToIListOps[A](self: IList[A]): IListOps[A] =
    new IListOps[A](self)
}
