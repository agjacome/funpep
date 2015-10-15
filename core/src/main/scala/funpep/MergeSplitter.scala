package funpep

import java.nio.file.Path
import java.util.UUID.randomUUID

import scalaz.concurrent._
import scalaz.stream._
import scalaz.stream.merge._
import scalaz.syntax.functor._

import data._
// import util.functions._
import util.ops.path._
import util.ops.foldable._


object MergeSplitter {

  def apply[A](cmp: Fasta[A], ref: Fasta[A], dir: Path)(implicit ev: A ⇒ Compound, st: Strategy): Process[Task, Path] =
    saveSplits(dir) { mergeSplit(cmp, ref) }

  def mergeSplit[A](cmp: Fasta[A], ref: Fasta[A])(implicit ev: A ⇒ Compound): Process[Task, Fasta[A]] =
    cmp.entries.toProcess.map(entry ⇒ Fasta(entry <:: ref.entries))

  def saveSplit[A](directory: Path)(split: Fasta[A]): Process[Task, Path] = {
    val path = directory / randomUUID.toString + ".fasta"
    split.toFile(path) >| path
  }

  def saveSplits[A](directory: Path)(splits: Process[Task, Fasta[A]])(implicit st: Strategy): Process[Task, Path] =
    mergeN { splits map saveSplit(directory) }

}
