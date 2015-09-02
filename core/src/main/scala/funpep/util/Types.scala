package funpep
package util

import scalaz._
import scalaz.concurrent._
import scalaz.stream._


private[util] trait Types {

  type ErrorMsg = String

  // FIXME: not working correctly with type lambdas, neither Process[F[_], ?].
  // Scala is not able to correctly resolve implicit values defined in
  // KleisliInstances with those types. I don't know why, but just defining the
  // PTask alias instead of using the lambda works.
  // type KleisliProcess[A, B]       = Kleisli[({ type λ[α] = Process[Task, α] })#λ, A, B]
  // type KleisliProcess[A, B[_], C] = Kleisli[({ type λ[α] = Process[B,    α] })#λ, A, C]
  type PTask[A] = Process[Task, A]
  type KleisliProcess[A, B] = Kleisli[PTask, A, B]

  object KleisliProcess extends KleisliInstances with KleisliFunctions {
    def apply[A, B](f: A ⇒ Process[Task, B]): KleisliProcess[A, B] =
      Kleisli[({ type λ[α] = Process[Task, α] })#λ, A, B](f)
  }

}
