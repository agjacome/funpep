// package es.uvigo.ei.sing.funpep
// package parser

// import java.io.{ Reader ⇒ JReader }
// import java.nio.file.Path

// import scala.util.parsing.combinator.RegexParsers

// import scalaz._
// import scalaz.syntax.bind._
// import scalaz.syntax.either._
// import scalaz.syntax.std.list._
// import scalaz.syntax.std.option._

// import data.{ FastaEntry, Fasta }


// object FastaParser extends RegexParsers {

  // val header   = ">.*".r    ^^ { _.tail.trim }
  // val seqLine  = "[^>].*".r ^^ { _.trim      }
  // val sequence = seqLine.+  ^^ { _.mkString  }

  // val entry = header ~ sequence ^^ { e ⇒ FastaEntry(e._1, e._2) }
  // val fasta = entry.+           ^^ { f ⇒ f.toNel.map(Fasta.apply) \/> "Could not parse as Fasta: no entries found." }

  // val parseString: String  ⇒ String ∨ Fasta = parseAll(fasta, _).either.join
  // val parseReader: JReader ⇒ String ∨ Fasta = parseAll(fasta, _).either.join

  // implicit class ParseResultOps[A](val parseResult: ParseResult[A]) extends AnyVal {

    // @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Throw"))
    // def fold[B](g: String ⇒ B)(f: A ⇒ B): B =
      // parseResult match {
        // case Success(res, _)   ⇒ f(res)
        // case NoSuccess(err, _) ⇒ g(err)
      // }

    // def either: String ∨ A = fold(_.left[A])(_.right[String])
    // def option: Option[A]  = either.toOption

  // }

// }

  // // def fromFile(file: Path): IOThrowable[Fasta] =
    // // EitherT { file.openIOReader.bracket(_.closeIO)(r ⇒ fromReader(r).point[IO]).catchLeft map (_.join) }

  // // def fromDirectory(directory: Path): IOThrowable[List[(Fasta, Path)]] =
    // // directory.files("*.{fasta,fas,fna,faa,ffn,frna}") >>= {
      // // files ⇒ files.traverse(fromFile).map(_.zip(files))
    // // }
