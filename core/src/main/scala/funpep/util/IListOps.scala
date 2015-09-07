package funpep
package util

import scalaz.{ IList, Maybe }
import scalaz.std.anyVal._
import scalaz.std.list._
import scalaz.syntax.foldable._
import scalaz.syntax.std.option._


final class IListOps[A] private[util] (val self: IList[A]) extends AnyVal {

  def tailMaybe: Maybe[IList[A]] = self.tailOption.toMaybe

  def grouped(n: Int): IList[IList[A]] =
    self.zipWithIndex.groupBy({
      case (a, i) ⇒ (i / n).toInt
    }).values.toIList.map(_.map(_._1))

}

trait ToIListOps {
  implicit def ToFunpepIListOps[A](self: IList[A]): IListOps[A] =
    new IListOps[A](self)
}
