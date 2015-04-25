package es.uvigo.ei.sing.funpep
package util

import java.io.{ BufferedReader, BufferedWriter, IOException }
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file._
import java.util.UUID

import scala.collection.JavaConverters._
import scala.sys.process.Process

import scalaz._
import scalaz.Scalaz._
import scalaz.effect._
import scalaz.iteratee._
import scalaz.iteratee.Iteratee._


object IOUtils {

  type ⇄[A] = EitherT[IO, Throwable, A]

  implicit def EitherIOToEitherTIO[A, B](e: ⇒ IO[A ∨ B])(implicit asThrowable: A ⇒ Throwable): ⇄[B] =
    EitherT(e) leftMap asThrowable


  def execute(command: String, args: String*): ⇄[String] =
    IO(Process(command, args)).map (_.!!).catchLeft


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
    def /(p: String): Path = path.resolve(p)
    def +(s: String): Path = Paths.get(path.toString + s)

    def create: ⇄[Unit] =
      IO(Files.createFile(path)) map (_ ⇒ ()) catchLeft

    def createDir: ⇄[Unit] =
      IO(Files.createDirectories(path)) map (_ ⇒ ()) catchLeft

    def delete: ⇄[Unit] =
      IO(Files.deleteIfExists(path)) map (_ ⇒ ()) catchLeft

    // TODO: !!!
    def deleteDir: ⇄[Unit] =
      IO(Files.walkFileTree(path, new SimpleFileVisitor[Path]() {
        override def visitFile(file: Path, attrs: attribute.BasicFileAttributes): FileVisitResult = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }

        override def postVisitDirectory(dir: Path, err: IOException): FileVisitResult = {
          Files.delete(dir)
          FileVisitResult.CONTINUE
        }
      })) map(_ ⇒ ()) catchLeft

    def openIOReader: IO[BufferedReader] = IO(Files.newBufferedReader(path, UTF_8))
    def openIOWriter: IO[BufferedWriter] = IO(Files.newBufferedWriter(path, UTF_8))

    def enumerateLines[A](action: IterateeT[IoExceptionOr[String], IO, A]): IO[A] =
      openIOReader.bracket(_.closeIO) { reader ⇒ (action &= reader.enumerateLines).run }

    def contentsAsString: ⇄[String] =
      enumerateLines(consume[IoExceptionOr[String], IO, List]) map {
        _.traverse(_.toOption).map(_ mkString ¶) \/> new IOException(s"Could not read contents of $path")
      }

    def files(glob: String): ⇄[List[Path]] =
      IO(Files.newDirectoryStream(path, glob)).bracket(_.close.point[IO]) {
        files ⇒ files.iterator.asScala.toList.point[IO]
      } catchLeft

  }

}
