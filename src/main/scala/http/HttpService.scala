package es.uvigo.ei.sing.funpep
package http

import java.net.URL

import scalaz.concurrent.Task

import org.http4s._
import org.http4s.dsl._
import org.http4s.server._

import com.typesafe.scalalogging.LazyLogging


final class HttpService (
  val assetsController:   AssetsController,
  val analyzerController: AnalyzerController
) extends LazyLogging {

  // Right-associative path extractor, already in http4s but not published
  private object /: {
    def unapply(path: Path): Option[(String, Path)] =
      unapply(path.toList)
    def unapply(list: List[String]): Option[(String, Path)] =
      list.headOption map { (_, Path(list.drop(1))) }
  }

  private def logRequest(req: Request): Unit =
    logger.info(s"${req.remoteAddr.getOrElse("noaddr")} ⇒ ${req.method}: ${req.uri.path} ${req.uri.query}")

  lazy val service = HttpService {

    case req @ GET -> Root ⇒
      assetsController.serve(req, Path("/index.html")).fold(NotFound())(Task.now)

    case req @ _ -> Root ⇒
      MethodNotAllowed()

    case req @ GET -> "assets" /: path ⇒
      logRequest(req)
      assetsController.serve(req, path).fold(NotFound())(Task.now)

  }

}

