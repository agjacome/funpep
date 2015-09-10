package funpep
package util

import scalaz._
import scalaz.concurrent._
import scalaz.syntax.show._

import types._


final class DisjunctionOps[A, B] private[util] (val self: A ∨ B) {

  def leftErr(implicit ev0: Show[A], ev1: A =!= Throwable): Throwable ∨ B =
    self.leftMap(a ⇒ sys.error(a.shows))

  def leftErr(msg: A ⇒ String)(implicit ev: A =!= Throwable): Throwable ∨ B =
    self.leftMap(a ⇒ sys.error(msg(a)))

  def toTask[T <: Throwable](implicit ev0: Show[A], ev1: A =!= T): Task[B] =
    Task.fromDisjunction(leftErr)

  def toTask[T <: Throwable](msg: A ⇒ String)(implicit ev: A =!= T): Task[B] =
    Task.fromDisjunction(leftErr(msg))

}

trait ToDisjunctionOps {
  implicit def ToFunpepDisjunctionOps[A, B](self: A ∨ B): DisjunctionOps[A, B] =
    new DisjunctionOps[A, B](self)
}
