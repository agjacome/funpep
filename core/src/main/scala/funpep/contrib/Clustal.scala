package funpep
package contrib

import java.nio.file.Path

import scala.sys.process.{ Process ⇒ Command }

import scalaz.stream._
import scalaz.concurrent._
import scalaz.syntax.apply._

import util.functions._
import util.types._
import util.ops.path._


object Clustal {

  private def clustalΩ(params: String): KleisliProcess[Path, String] =
    KleisliProcess {
      clustalo ⇒ Process.eval { Task.delay(Command(s"$clustalo $params --force")!!) }
    }

  def distanceMatrix(input: Path, alignment: Path, distmat: Path): KleisliProcess[Path, Unit] =
    clustalΩ(s"-i $input -o $alignment --distmat-out=$distmat --percent-id --full").map(discard)

  def guideTree(input: Path, alignment: Path, tree: Path): KleisliProcess[Path, Unit] =
    clustalΩ(s"-i $input -o $alignment --guidetree-out=$tree --full").map(discard)

  def withDistanceMatrixOf[A](input: Path)(f: Path ⇒ Process[Task, A]): KleisliProcess[Path, A] = {
    val distmat = input + ".distmat"

    def written = distanceMatrix(input, devnull, distmat)
    def mapped  = KleisliProcess[Path, A](_ ⇒ f(distmat))

    written *> mapped
  }

}
