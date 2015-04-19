package es.uvigo.ei.sing

import java.io.{ BufferedReader, BufferedWriter, IOException }
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{ Files, Path, Paths }

import scala.collection.JavaConverters._
import scala.sys.process.Process

import scalaz._
import scalaz.Scalaz._
import scalaz.effect._
import scalaz.iteratee._
import scalaz.iteratee.Iteratee._


package object funpep {

  type IoEitherT[A, B] = EitherT[IO, A, B]
  type ErrorOrIO[A]    = IoEitherT[Throwable, A]

  lazy val ¶ = System.lineSeparator

  implicit class BufferedReaderOps(val reader: BufferedReader) extends AnyVal {
    def closeIO:    IO[Unit]           = reader.close.point[IO]
    def readLineIO: IO[Option[String]] = Option(reader.readLine).point[IO]

    def enumerateLines: EnumeratorT[IoExceptionOr[String], IO] =
      enumIoSource[Option[String], String, IO](
        get     = ()   ⇒ IoExceptionOr { Option(reader.readLine) },
        gotdata = line ⇒ line.exists(_.isDefined),
        render  = line ⇒ line.err("Unexpected error while reading line")
      )

    def bracket[A](after: ⇒ BufferedReader ⇒ Unit)(during: ⇒ BufferedReader ⇒ A): A = {
      val res = during(reader)
      after(reader)
      res
    }
  }

  implicit class BufferedWriterOps(val writer: BufferedWriter) extends AnyVal {
    def closeIO: IO[Unit] = writer.close.point[IO]
    def writeIO(str: String): IO[Unit] = writer.write(str).point[IO]

    def bracket[A](after: ⇒ BufferedWriter ⇒ Unit)(during: ⇒ BufferedWriter ⇒ A): A = {
      val res = during(writer)
      after(writer)
      res
    }
  }

  implicit class PathOps(val path: Path) extends AnyVal {
    def /(p: Path  ): Path = path.resolve(p)
    def +(s: String): Path = Paths.get(path.toString + s)

    def delete: IO[Unit] = Files.deleteIfExists(path).point[IO] map { _ ⇒ () }

    def openIOReader: IO[BufferedReader] = Files.newBufferedReader(path, UTF_8).point[IO]
    def openIOWriter: IO[BufferedWriter] = Files.newBufferedWriter(path, UTF_8).point[IO]

    def enumerateLines[A](action: IterateeT[IoExceptionOr[String], IO, A]): IO[A] =
      openIOReader.bracket(_.closeIO) { reader ⇒ (action &= reader.enumerateLines).run }

    def contentsAsString: ErrorOrIO[String] =
      EitherT(enumerateLines(consume[IoExceptionOr[String], IO, List]) map {
        _.traverse(_.toOption).map(_ mkString ¶) \/> new IOException(s"Could not read contents of $path")
      })

    def files(glob: String): ErrorOrIO[List[Path]] = {
      val stream = Files.newDirectoryStream(path, glob)
      EitherT(stream.point[IO].bracket(_.close.point[IO]) {
        files ⇒ files.iterator.asScala.toList.point[IO]
      } catchLeft)
    }
  }

  implicit class StringOps(val str: String) extends AnyVal {
    def toPath: Path = Paths.get(str)
  }

  implicit class PathInterpolator(val sc: StringContext) extends AnyVal {
    def path(args: Any*): Path = Paths.get(sc.s(args: _*))
  }

  implicit def stringToCaseInsensitive(str: String): CaseInsensitive[String] =
    CaseInsensitive(str)

  def uuid: String = java.util.UUID.randomUUID.toString

  def execute(command: String, args: String*): ErrorOrIO[String] =
    EitherT { Process(command, args).point[IO].map(_.!!).catchLeft }

  def resource(resource: String): java.net.URL =
    Option(Thread.currentThread.getContextClassLoader) err {
      "Context classloader is not set for the current thread."
    } getResource resource

}
