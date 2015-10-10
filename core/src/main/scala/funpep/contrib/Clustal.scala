package funpep
package contrib

import java.nio.file.Path

import scala.sys.process.{ Process ⇒ Command }

import scalaz.stream._
import scalaz.concurrent._
import scalaz.syntax.applicative._
import scalaz.syntax.kleisli._

import util.functions._
import util.types._
import util.ops.path._


object Clustal {

  private def clustalΩ(params: String): KleisliP[Path, String] =
    KleisliP { clustalo ⇒ AsyncP(Command(s"$clustalo $params --force")!!) }

  def distanceMatrix(input: Path, alignment: Path, distmat: Path): KleisliP[Path, Unit] =
    clustalΩ(s"-i $input -o $alignment --distmat-out=$distmat --percent-id --full").void

  def guideTree(input: Path, alignment: Path, tree: Path): KleisliP[Path, Unit] =
    clustalΩ(s"-i $input -o $alignment --guidetree-out=$tree --full").void

  def withDistanceMatrixOf[A](input: Path)(f: Path ⇒ Process[Task, A]): KleisliP[Path, A] = {
    val distmat = input + ".distmat"
    distanceMatrix(input, devnull, distmat) *> f(distmat).liftKleisli
  }

}
