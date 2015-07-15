package es.uvigo.ei.sing.funpep
package data

import java.nio.file.Path

import scalaz._
import scalaz.Scalaz._

final case class Config (
  httpHost: String,
  httpPort: Int,
  httpPath: String,
  clustalo: String,
  database: Path,
  jobQueue: Path
)

object Config {

  type ConfiguredT[F[_], A] = ReaderT[F, Config, A]
  type Configured[A]        = ConfiguredT[Id, A]

  def ConfiguredT[F[_], A](f: Config ⇒ F[A]): ConfiguredT[F, A] =
    Kleisli[F, Config, A](f)

  def Configured[A](f: Config ⇒ A): Configured[A] =
    Kleisli[Id, Config, A](f)

}
