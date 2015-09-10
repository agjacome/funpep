package funpep
package util

import scalaz._
import scalaz.concurrent._
import scalaz.stream._


private[util] trait Types {

  // Distinct type notation, usage:
  //
  //   def foo[A, B](implicit ev: A =!= B): Unit = ???
  //
  // Compiler message is quite horrendous when evidence cannot be proven (eg:
  // implicitly[Int =!= Int]), because it is implemented relying on a simple
  // trick with ambiguous implicits. This will be fixed in Scala 2.12 as per
  // SI-6806 @implicitAmbiguous annotation.
  trait =!=[A, B]
  implicit def neq[A, B]: A =!= B = null
  implicit def neqAmbig0[A, B, C]: C =!= C = null
  implicit def neqAmbig1[A]: A =!= A = null

  type ErrorMsg = String

  // FIXME: not working correctly with type lambdas, neither Process[F[_], ?].
  // Scala is not able to correctly resolve implicit values defined in
  // KleisliInstances with those types. I don't know why, but just defining the
  // P alias instead of using the lambda works.
  // type KleisliP[A, B]       = Kleisli[({ type λ[α] = Process[Task, α] })#λ, A, B]
  // type KleisliP[A, B[_], C] = Kleisli[({ type λ[α] = Process[B,    α] })#λ, A, C]
  protected type P[A] = Process[Task, A]
  type KleisliP[A, B] = Kleisli[P, A, B]

  object KleisliP extends KleisliInstances with KleisliFunctions {
    def apply[A, B](f: A ⇒ Process[Task, B]): KleisliP[A, B] =
      Kleisli[({ type λ[α] = Process[Task, α] })#λ, A, B](f)
  }

}
