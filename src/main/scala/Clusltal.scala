package es.uvigo.ei.sing.funpep

import java.nio.file.Path

import scalaz._
import scalaz.effect.IO


object Clustal {

  // TODO: this should be configurable, not hard-coded
  lazy val clustalo = "/usr/bin/clustalo"

  def distanceMatrix(input: Path, alignment: Path, distmat: Path): IO[Throwable \/ String] =
    execute(clustalo, s"-i $input", s"-o $alignment", "--percent-id", "--full", s"--distmat-out=$distmat", "--force")

  def guideTree(input: Path, alignment: Path, tree: Path): IO[Throwable \/ String] =
    execute(clustalo, s"-i $input", s"-o $alignment", "--full", s"--guidetree-out=$tree", "--force")

}
