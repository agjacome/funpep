package funpep

import java.nio.file.Path
import java.util.UUID.randomUUID

import scalaz._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.syntax.foldable._

import data._
import util.ops.path._
import util.ops.foldable._


object MergeSplitter {

  def mergeSplit[A](cmp: Fasta[A], ref: Fasta[A])(implicit ev: A ⇒ Compound): EphemeralStream[Fasta[A]] =
    cmp.entries.toEphemeralStream.map(entry ⇒ Fasta(entry <:: ref.entries))

  def saveSplits[A, F[_]: Foldable](directory: Path)(splits: F[Fasta[A]]): Process[Task, Unit] =
    merge.mergeN(splits.toProcess map {
      _.toFile(directory / randomUUID.toString + ".fasta")
    })

}
