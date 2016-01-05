package funpep
package util

import java.util.StringJoiner

import scala.annotation.unchecked.uncheckedVariance

import scalaz._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.syntax.show._

import Liskov.{ <~<, refl }

final class FoldableOps[F[_], A] private[util] (val self: F[A])(implicit val F: Foldable[F]) {

  def mkString(start: String, sep: String, end: String)(implicit ev1: Show[A]): String =
    F.foldLeft(self, new StringJoiner(sep, start, end)) {
      (sj, a) ⇒ sj.add(a.shows)
    } toString

  def mkString(sep: String)(implicit ev: Show[A]): String =
    mkString("", sep, "")

  def mkString(implicit ev: Show[A]): String =
    mkString("")

  def mkString(converter: A ⇒ String, start: String, sep: String, end: String): String =
    mkString(start, sep, end)(Show.shows(converter))

  def mkString(converter: A ⇒ String, sep: String): String =
    mkString(converter, "", sep, "")

  def mkString(converter: A ⇒ String): String =
    mkString(converter, "")

  def toMap[K, V](implicit ev: A <~< (K, V), ord: Order[K]): K ==>> V = {
    val widen = ev.subst[({ type λ[-α] = F[α @uncheckedVariance] <~< F[(K, V)] })#λ](refl)(self)
    F.foldLeft(widen, ==>>.empty[K, V])(_ + _)
  }

  def toISet(implicit ord: Order[A]): ISet[A] =
    ISet.fromFoldable(self)

  def toProcess: Process[Task, A] =
    Process.emitAll { F.toList(self) }

}

trait ToFoldableOps {
  implicit def ToFunpepFoldableOps[F[_]: Foldable, A](self: F[A]): FoldableOps[F, A] =
    new FoldableOps[F,A](self)
}
