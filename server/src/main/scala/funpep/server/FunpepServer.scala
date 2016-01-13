package funpep.server

import scala.concurrent.ExecutionContext.Implicits._

import scalaz.concurrent._
import scalaz.stream._
import scalaz.syntax.functor._

import org.http4s.server._
import org.http4s.server.blaze._

import funpep._
import funpep.data._
import funpep.util.all._


object FunpepServer {

  // TODO: get threads as optional main argument, default to processors
  implicit lazy val strategy = fixedPoolStrategy(processors)

  // TODO: hardcoded values, receive as required main arguments and validate
  lazy val httpPort = 65480
  lazy val httpPath = "/funpep"
  lazy val clustalo = "/usr/bin/clustalo".toPath
  lazy val database = "/home/agjacome/funpep/database".toPath

  def router(queue: AnalyzerQueue[AminoAcid]): HttpService =
    Router(
      "/analysis" → AnalyzerService[AminoAcid](queue),
      "/dataset"  → DatasetService()
    )

  def runServer(queue: AnalyzerQueue[AminoAcid]): Process[Task, Unit] =
    AsyncP { BlazeBuilder.bindHttp(httpPort).mountService(router(queue), httpPath).run.awaitShutdown }

  def runAnalyzer(queue: AnalyzerQueue[AminoAcid]): Process[Task, Unit] =
    queue.analyzerLoop.apply(clustalo).void

  def main(args: Array[String]): Unit = {
    val main = for {
      _ ← database.createDir
      q ← AnalyzerQueue(Analyzer[AminoAcid](database))
      m ← runServer(q).merge(runAnalyzer(q))
    } yield m

    main.run.run
  }

}
