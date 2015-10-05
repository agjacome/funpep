package funpep

import java.nio.file.Path
import java.util.UUID.randomUUID

import scalaz._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.syntax.foldable._

import data._
import util.functions._
import util.ops.path._
import util.ops.foldable._


object MergeSplitter {

  def apply[A](cmp: Fasta[A], ref: Fasta[A], dir: Path)(implicit ev: A ⇒ Compound): Process[Task, Path] =
    saveSplits(dir) { mergeSplit(cmp, ref) }

  def mergeSplit[A](cmp: Fasta[A], ref: Fasta[A])(implicit ev: A ⇒ Compound): EphemeralStream[Fasta[A]] =
    cmp.entries.toEphemeralStream.map(entry ⇒ Fasta(entry <:: ref.entries))

  def saveSplit[A](directory: Path)(split: Fasta[A]): Process[Task, Path] = {
    val path = directory / randomUUID.toString + ".fasta"
    split.toFile(path).map(_ ⇒ path)
  }

  def saveSplits[A, F[_]: Foldable](directory: Path)(splits: ⇒ F[Fasta[A]]): Process[Task, Path] =
    MergeN(splits.toProcessDelay map saveSplit(directory))

}
