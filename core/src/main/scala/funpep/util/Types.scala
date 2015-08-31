package funpep
package util

import scalaz._
import scalaz.stream._


private[util] trait Types {

  type ErrorMsg = String

  // TODO: use kind-projector
  type ReaderP[A, B[_], C] = ReaderT[({ type λ[Z] = Process[B, Z] })#λ, A, C]

}

object types extends Types
