package funpep
package util

import scalaz._
import scalaz.concurrent._
import scalaz.stream._


private[util] trait Types {

  trait =!=[A, B]
  implicit def neq[A, B]: A =!= B = null
  implicit def neqAmbig0[A, B, C]: C =!= C = null
  implicit def neqAmbig1[A]: A =!= A = null

  type ErrorMsg = String

  protected type P[A] = Process[Task, A]
  type KleisliP[A, B] = Kleisli[P, A, B]

  object KleisliP extends KleisliInstances with KleisliFunctions {
    def apply[A, B](f: A ⇒ Process[Task, B]): KleisliP[A, B] =
      Kleisli[Process[Task, ?], A, B](f)
  }

}
