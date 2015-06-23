package es.uvigo.ei.sing.funpep
package util

import java.io._
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file._
import java.util.UUID

import scala.collection.JavaConverters._
import scala.sys.process.Process

import com.typesafe.scalalogging.LazyLogging

import scalaz._
import scalaz.Scalaz._
import scalaz.effect._
import scalaz.iteratee._
import scalaz.iteratee.Iteratee._


object IOUtils extends LazyLogging {

  type IOEitherT[A, B] = EitherT[IO, A, B]
  type IOThrowable[A]  = IOEitherT[Throwable, A]

  implicit def IOEitherToIOThrowable[A, B](e: ⇒ IO[A ∨ B])(implicit asT: A ⇒ Throwable): IOThrowable[B] =
    EitherT(e) leftMap asT


  def execute(command: String): IOThrowable[String] = {
    logger.info(s"Executing command: $command")
    IO(Process(command)).map (_.!!).catchLeft
  }


  implicit class CloseableOps(val closeable: Closeable) extends AnyVal {
    def closeIO: IO[Unit] = closeable.close.point[IO]
  }

  implicit class InputStreamOps(val input: InputStream) extends AnyVal {
    def toReader: BufferedReader = new BufferedReader(new InputStreamReader(input, UTF_8))
  }

  implicit class BufferedReaderOps(val reader: BufferedReader) extends AnyVal {

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

    def create: IOThrowable[Unit] =
      IO(Files.createFile(path)) map (_ ⇒ ()) catchLeft

    def createDir: IOThrowable[Unit] =
      IO(Files.createDirectories(path)) map (_ ⇒ ()) catchLeft

    def delete: IOThrowable[Unit] =
      IO(Files.deleteIfExists(path)) map (_ ⇒ ()) catchLeft

    // TODO: Java's FileVisitor is quite cumbersome, find a simpler way
    def deleteDir: IOThrowable[Unit] =
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

    def openIOIStream: IO[InputStream]    = IO(Files.newInputStream(path))
    def openIOOStream: IO[OutputStream]   = IO(Files.newOutputStream(path))
    def openIOReader:  IO[BufferedReader] = IO(Files.newBufferedReader(path, UTF_8))
    def openIOWriter:  IO[BufferedWriter] = IO(Files.newBufferedWriter(path, UTF_8))

    def enumerateLines[A](action: IterateeT[IoExceptionOr[String], IO, A]): IO[A] =
      openIOReader.bracket(_.closeIO) { reader ⇒ (action &= reader.enumerateLines).run }

    def contentsAsList: IOThrowable[List[String]] =
      enumerateLines(consume[IoExceptionOr[String], IO, List]) map {
        _.traverse(_.toOption) \/> new IOException(s"Could not read contents of $path")
      }

    def contentsAsString: IOThrowable[String] =
      contentsAsList map { _ mkString nl }

    def files(glob: String): IOThrowable[List[Path]] =
      IO(Files.newDirectoryStream(path, glob)).bracket(_.close.point[IO]) {
        files ⇒ files.iterator.asScala.toList.point[IO]
      } catchLeft

  }

}
