package es.uvigo.ei.sing.funpep

import java.nio.file.Path

import scalaz.Scalaz._


object Splitter {

  def split(comparing: Fasta, reference: Fasta): List[Fasta] =
    comparing.entries.map(e ⇒ Fasta(e <:: reference.entries)).toList

  def splitAndSaveTo(directory: Path)(comparing: Fasta, reference: Fasta): ErrorOrIO[Unit] =
    FastaPrinter.toDirectory(directory) { split(comparing, reference) }

}
