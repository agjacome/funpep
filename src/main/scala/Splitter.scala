package es.uvigo.ei.sing.funpep

import scalaz.Scalaz._


object Splitter extends ((Fasta, Fasta) ⇒ Stream[Fasta]) {

  def split(comparing: Fasta, reference: Fasta): Stream[Fasta] =
    comparing.entries.toStream map { Fasta(_, reference.entries.toList: _*) }

  override def apply(comparing: Fasta, reference: Fasta): Stream[Fasta] =
    split(comparing, reference)

}
