package es.uvigo.ei.sing.funpep

import java.nio.file.Path

import scalaz._
import scalaz.effect.IO

import Config._

object Clustal {

  def distanceMatrix(input: Path, alignment: Path, distmat: Path): Configured[IO[Throwable \/ String]] =
    withConfig { config ⇒
      execute(config.clustalo.toString, s"-i $input -o $alignment --distmat-out=$distmat --percent-id --full --force")
    }

  def guideTree(input: Path, alignment: Path, tree: Path): Configured[IO[Throwable \/ String]] =
    withConfig { config ⇒
      execute(config.clustalo.toString, s"-i $input -o $alignment --guidetree-out=$tree --full --force")
    }

}
