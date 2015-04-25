package es.uvigo.ei.sing.funpep
package contrib

import java.nio.file.Path

import scalaz.Scalaz._

import data.Config.syntax._
import util.IOUtils._


object Clustal {

  def distanceMatrix(input: Path, alignment: Path, distmat: Path): ConfiguredT[⇄, String] =
    ConfiguredT {
      config ⇒ execute(s"${config.clustalo} -i $input -o $alignment --distmat-out=$distmat --percent-id --full --force")
    }

  // TODO: clean up
  def withDistanceMatrixOf[A](input: Path)(f: Path ⇒ ⇄[A]): ConfiguredT[⇄, A] = {
    lazy val distMat = input + ".distmat"
    lazy val written = ConfiguredT(config ⇒ distanceMatrix(input, config.nullPath, distMat)(config))
    lazy val mapped  = f(distMat).liftM[ConfiguredT]
    written *> mapped
  }

  def guideTree(input: Path, alignment: Path, tree: Path): ConfiguredT[⇄, String] =
    ConfiguredT {
      config ⇒ execute(s"${config.clustalo} -i $input -o $alignment --guidetree-out=$tree --full --force")
    }

}
