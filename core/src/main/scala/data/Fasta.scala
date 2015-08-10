// package es.uvigo.ei.sing.funpep
// package data

// import scalaz._
// import scalaz.std.string._
// import scalaz.syntax.order._
// import scalaz.syntax.show._


// final case class FastaEntry (
  // header:   FastaEntry.Header,
  // sequence: CaseInsensitive[String]
// )

// object FastaEntry {

  // type Header = String

  // def apply(header: Header, sequence: String): FastaEntry =
    // new FastaEntry(header, CaseInsensitive(sequence))

  // implicit val FastaEntryOrder: Order[FastaEntry] = new Order[FastaEntry] {
    // override def order(e1: FastaEntry, e2: FastaEntry): Ordering =
      // e1.sequence ?|? e2.sequence
  // }

  // implicit val FastaEntryShow: Show[FastaEntry] = new Show[FastaEntry] {
    // override def shows(e: FastaEntry): String = ">" + e.header + nl + e.sequence.original.grouped(70).mkString(nl)
  // }

// }

// final case class Fasta (entries: NonEmptyList[FastaEntry])

// object Fasta {

  // def apply(head: FastaEntry, tail: FastaEntry*): Fasta =
    // new Fasta(NonEmptyList(head, tail: _*))

  // implicit val FastaSemigroup: Semigroup[Fasta] = new Semigroup[Fasta] {
    // override def append(f1: Fasta, f2: ⇒ Fasta): Fasta =
      // Fasta(f1.entries.append(f2.entries))
  // }

  // implicit val FastaEqual: Equal[Fasta] = new Equal[Fasta] {
    // override def equal(f1: Fasta, f2: Fasta): Boolean =
      // f1.entries.sorted.distinct ≟ f2.entries.sorted.distinct
  // }

  // implicit val FastaShow: Show[Fasta] = new Show[Fasta] {
    // override def shows(f: Fasta): String =
      // f.entries.list.map(_.shows).mkString(nl)
  // }

// }

// // object FastaPrinter {

  // // import scalaz.\/.{ fromTryCatchNonFatal ⇒ tryCatch }

  // // lazy val toWriter: BufferedWriter ⇒ Fasta ⇒ Throwable ∨ Unit =
    // // writer ⇒ fasta ⇒ tryCatch { writer.write(fasta.toFastaString) }

  // // def toFile(file: Path)(fasta: ⇒ Fasta): IOThrowable[Unit] =
    // // file.openIOWriter.bracket(_.closeIO) { toWriter(_)(fasta).point[IO] }

  // // def toNewFile(directory: Path)(fasta: ⇒ Fasta): IOThrowable[Unit] =
    // // toFile(directory / uuid.toString + ".fasta")(fasta)

  // // def toDirectory(directory: Path)(fastas: ⇒ List[Fasta]): IOThrowable[Unit] =
    // // fastas traverse { f ⇒ toNewFile(directory)(f) } map { _ ⇒ () }

// // }
