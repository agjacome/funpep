package funpep
package util

import java.util.StringJoiner

import scalaz._
import scalaz.syntax.show._

final class FoldableOps[F[_], A] private[util] (val self: F[A])(implicit val F: Foldable[F]) {

  def mkString(start: String, sep: String, end: String)(implicit ev: Show[A]): String =
    F.foldLeft(self, new StringJoiner(sep, start, end)) {
      (sj, a) ⇒ sj.add(a.shows)
    } toString

  def mkString(sep: String)(implicit ev: Show[A]): String = mkString("", sep, "")
  def mkString             (implicit ev: Show[A]): String = mkString("", "",  "")

}

trait ToFoldableOps {
  implicit def ToFoldableOps[F[_], A](self: F[A])(implicit F: Foldable[F]): FoldableOps[F, A] =
    new FoldableOps[F,A](self)
}
