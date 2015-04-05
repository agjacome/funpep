package es.uvigo.ei.sing

import java.io.{ BufferedReader, BufferedWriter }
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{ Files, Path, Paths }
import java.util.UUID

import scala.collection.JavaConverters.asScalaIteratorConverter

import scalaz._
import scalaz.Scalaz._
import scalaz.effect._
import scalaz.iteratee._
import scalaz.iteratee.Iteratee._


package object funpep {

  lazy val ¶ = System.lineSeparator

  implicit class BufferedReaderOps(val reader: BufferedReader) extends AnyVal {
    def closeIO:    IO[Unit]           = reader.close.point[IO]
    def readLineIO: IO[Option[String]] = Option(reader.readLine).point[IO]

    def enumerateLines: EnumeratorT[IoExceptionOr[String], IO] =
      enumIoSource(
        get     = () ⇒ IoExceptionOr { Option(reader.readLine) },
        gotdata = (l: IoExceptionOr[Option[String]]) ⇒ l exists (_.isDefined),
        render  = (l: Option[String]) ⇒ l.err("Unexpected error while reading line")
      )
  }

  implicit class BufferedWriterOps(val writer: BufferedWriter) extends AnyVal {
    def closeIO: IO[Unit] = writer.close.point[IO]
    def writeIO(str: String): IO[Unit] = writer.write(str).point[IO]
  }

  implicit class PathOps(val path: Path) extends AnyVal {
    def /(p: Path): Path = path.resolve(p)

    def openIOReader: IO[BufferedReader] = Files.newBufferedReader(path, UTF_8).point[IO]
    def openIOWriter: IO[BufferedWriter] = Files.newBufferedWriter(path, UTF_8).point[IO]

    def enumerateLines[A](action: IterateeT[IoExceptionOr[String], IO, A]): IO[A] =
      openIOReader.bracket(_.closeIO) { reader ⇒ (action &= reader.enumerateLines).run }

    def files(glob: String): IO[List[Path]] = {
      val stream = Files.newDirectoryStream(path, glob)
      stream.point[IO].bracket(_.close.point[IO]) {
        files ⇒ files.iterator.asScala.toList.point[IO]
      }
    }
  }

  implicit class PathInterpolator(val sc: StringContext) extends AnyVal {
    def path(args: Any*): Path = Paths.get(sc.s(args: _*))
  }

  implicit def stringToCaseInsensitive(s: String): CaseInsensitive[String] =
    CaseInsensitive(s)

  def uuid: UUID = UUID.randomUUID

}
