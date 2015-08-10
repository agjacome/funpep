package es.uvigo.ei.sing

import java.util.UUID

import scalaz._
import scalaz.syntax.show._
import scalaz.syntax.std.option._

// FIXME; Check why SBT sometimes refuses to compile this unit, without any
// warning or error. It's like the file does not exist in those cases.
package object funpep {

  def randomUUID: UUID = UUID.randomUUID

  implicit final class StringOps(val s: String) extends AnyVal {
    def words:   IList[String] = IList(s.split("\\s+"): _*)
    def toIList: IList[Char]   = IList(s: _*)
  }

  implicit final class IListOps[A](val as: IList[A]) extends AnyVal {
    def tailMaybe: Maybe[IList[A]] = as.tailOption.toMaybe

    def grouped(n: Int): IList[IList[A]] = {
      def groups = Math.ceil(as.length / n.toDouble).toInt
      Stream.from(0, n).take(groups).foldRight(IList.empty[IList[A]]) {
        (index, chunks) ⇒ as.slice(index, index + n) :: chunks
      }
    }

    def mkString(sep: String)(implicit ev: Show[A]): Maybe[String] =
      as.map(_.shows).intersperse(sep).reduceRightOption(_ + _).toMaybe
  }

}
