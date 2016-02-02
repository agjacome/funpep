package funpep.server

import java.nio.file.{ Files, Path }

import scalaz._
import scalaz.syntax.apply._

import net.bmjames.opts._

import funpep.util.types._
import funpep.util.functions._
import funpep.util.ops.string._


final case class Options (
  val numThreads: Int,
  val httpPort:   Int,
  val httpPath:   String,
  val clustalo:   Path,
  val database:   Path
)

object Options {

  type V[A]       = ValidationNel[ErrorMsg, A]
  type ParserV[A] = Parser[V[A]]

  private implicit final class BooleanOps(b: Boolean) {
    def foldV[A](a: ⇒ A, msg: ⇒ ErrorMsg): V[A] =
      if (b) Validation.success(a) else Validation.failureNel(msg)
  }

  private implicit val ParserVInstance: Applicative[ParserV] =
    Applicative[Parser] compose Applicative[V]


  def validNumThreads(threads: Int): V[Int] = {
    val maxThreads = processors * 30
    (threads >= 1 && threads <= maxThreads).foldV(threads, s"Threads number must be in range [1,$maxThreads]. $threads given")
  }

  def validHttpPort(port: Int): V[Int] =
    (port >= 1 && port <= 65535).foldV(port, s"HTTP Port must be in range [1,65535]. $port given")

  def validHttpPath(path: String): V[String] =
    path.startsWith("/").foldV(path, s"HTTP Path must start with '/'. $path given")

  def validClustalo(clustalo: Path): V[Path] =
    Files.exists(clustalo).foldV(clustalo, s"ClustalΩ not found at $clustalo")

  def validDatabase(database: Path): V[Path] =
    Files.isDirectory(database).foldV(database, s"Database path not found at $database")

  val numThreads: ParserV[Int]    = intOption(long("threads")).map(validNumThreads)
  val httpPort:   ParserV[Int]    = intOption(long("http-port")).map(validHttpPort)
  val httpPath:   ParserV[String] = strOption(long("http-path")).map(validHttpPath)
  val clustalo:   ParserV[Path]   = strOption(long("clustalo")).map(_.toPath).map(validClustalo)
  val database:   ParserV[Path]   = strOption(long("database")).map(_.toPath).map(validDatabase)

  val options: ParserV[Options] =
    (numThreads ⊛ httpPort ⊛ httpPath ⊛ clustalo ⊛ database)(Options.apply)

}
