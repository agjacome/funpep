package es.uvigo.ei.sing.funpep

import scala.concurrent.ExecutionContext.Implicits.global

import scalaz.Scalaz._
import scalaz.effect.IO.{ putStrLn, readLn }

import org.http4s.server.Server
import org.http4s.server.blaze.BlazeBuilder

import com.typesafe.scalalogging.LazyLogging

import data.Config
import http._
import util.IOUtils.property


// TODO: ugly code, clean up
object Funpep extends LazyLogging {

  lazy val config = property("config.file").fold(Config.default) {
    path ⇒ Config(path.toPath).run.unsafePerformIO() valueOr { err ⇒
      logger.error(s"Error parsing config file $path: ${err.getMessage}")
      sys.exit(-1)
    }
  }

  def startAnalyzer(config: Config): Analyzer = {
    logger.info(s"Starting analyzer - db: ${config.database}, temp: ${config.temporal}, queue: ${config.jobQueue}")
    val analyzer = Analyzer(config)
    analyzer.start()
    analyzer
  }

  def startServer(config: Config, service: Service): Server = {
    logger.info(s"Starting server at http://${config.httpHost}:${config.httpPort}${config.httpPath}")
    BlazeBuilder
      .mountService(service.router, config.httpPath)
      .bindHttp(config.httpPort, config.httpHost)
      .withNio2(true)
      .run
  }

  def main(args: Array[String]): Unit = {
    logger.info("Funpep started")

    putStrLn(
      s"""|Starting funpep at http://${config.httpHost}:${config.httpPort}${config.httpPath}
          |Press ENTER to terminate""".stripMargin
    ).unsafePerformIO()

    val analyzer = startAnalyzer(config)
    val server   = startServer(config, new Service(
      new AssetsController, new AnalyzerController(analyzer)
    ))

    (readLn *> putStrLn("Stopping funpep...")).unsafePerformIO()

    val _ = server.shutdownNow()
    analyzer.stop()

    logger.info("Funpep terminated")
  }

}
