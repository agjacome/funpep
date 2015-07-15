package es.uvigo.ei.sing.funpep
package contrib

import java.nio.file.Path

import scalaz.Scalaz._

import data.Config._
import util.IOUtils._


object Clustal {

  val devNull = "/dev/null".toPath

  def distanceMatrix(input: Path, alignment: Path, distmat: Path): ConfiguredT[IOThrowable, String] =
    ConfiguredT {
      config ⇒ execute(s"${config.clustalo} -i $input -o $alignment --distmat-out=$distmat --percent-id --full --force")
    }

  def guideTree(input: Path, alignment: Path, tree: Path): ConfiguredT[IOThrowable, String] =
    ConfiguredT {
      config ⇒ execute(s"${config.clustalo} -i $input -o $alignment --guidetree-out=$tree --full --force")
    }

  def withDistanceMatrixOf[A](input: Path)(f: Path ⇒ IOThrowable[A]): ConfiguredT[IOThrowable, A] = {
    val distMat = input + ".distmat"
    def written = distanceMatrix(input, devNull, distMat)
    def mapped  = f(distMat).liftM[ConfiguredT]
    written *> mapped
  }

}
