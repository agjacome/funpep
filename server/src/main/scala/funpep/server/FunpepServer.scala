package funpep.server

import scala.concurrent.ExecutionContext.Implicits._

import scalaz._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.syntax.functor._

import org.http4s._
import org.http4s.server.middleware._
import org.http4s.server.blaze._

import net.bmjames.opts.{ execParser, info }

import funpep._
import funpep.data._
import funpep.util.functions._
import funpep.util.types._
import funpep.util.ops.foldable._

import service._

object FunpepServer {

  def main(args: Array[String]): Unit = {
    parseArgs(args).fold(showErrors, runFunpep).run[Task].run
  }

  def showErrors(errors: NonEmptyList[ErrorMsg]): Process[Task, Unit] =
    AsyncP {
      val errStr = errors.mkString(identity, "\n")
      System.err.println(errStr)
    }

  // using mapK and flatMap in here is even uglier than this ".apply" stuff
  def runFunpep(options: Options): Process[Task, Unit] =
    analyzerQueue.apply(options).flatMap[Task, Unit] { queue ⇒
      val server   = runServer(queue).apply(options)
      val analyzer = runAnalyzer(queue).apply(options)

      server.merge(analyzer)
    }

  private def parseArgs(args: Array[String]): ValidationNel[ErrorMsg, Options] =
    execParser(args, "funpep-server", info(Options.options))

  private def analyzerQueue: KleisliP[Options, AnalyzerQueue[AminoAcid]] =
    KleisliP { options ⇒
      implicit val strategy = fixedPoolStrategy(options.numThreads)
      AnalyzerQueue(Analyzer[AminoAcid](options.database))
    }

  private def httpRouter(queue: AnalyzerQueue[AminoAcid]): HttpService =
    RouterService.service(
      s ⇒ CORS(AutoSlash(s)),
      AnalyzerService(queue),
      DatasetService()
    )

  private def runServer(queue: AnalyzerQueue[AminoAcid]): KleisliP[Options, Unit] =
    KleisliP { options ⇒
      val router = httpRouter(queue)
      val server = BlazeBuilder.bindHttp(options.httpPort).mountService(router, options.httpPath)

      AsyncP(server.run.awaitShutdown)
    }

  private def runAnalyzer(queue: AnalyzerQueue[AminoAcid]): KleisliP[Options, Unit] =
    KleisliP { options ⇒ queue.analyzerLoop.apply(options.clustalo).void }

}
