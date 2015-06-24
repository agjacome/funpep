package es.uvigo.ei.sing.funpep
package http

import java.util.UUID

import scalaz.concurrent.Task
import scalaz.\/.{ fromTryCatchThrowable ⇒ tryCatch }

import org.http4s._
import org.http4s.argonaut._
import org.http4s.dsl._
import org.http4s.server._

import com.typesafe.scalalogging.LazyLogging

import es.uvigo.ei.sing.funpep.util.JsonUtils._


final class ApplicationService (
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

  private def logRequest(req: Request): Request = {
    logger.info(s"${req.remoteAddr.getOrElse("noaddr")} ⇒ ${req.method}: ${req.uri.path} ${req.uri.query}")
    req
  }

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Throw"))
  def service: HttpService =
    HttpService(PartialFunction(logRequest _) andThen {

      // funpep analyzer API
      case GET -> Root / "api" / "queue_size" ⇒
        analyzerController.queueSize

      case GET -> Root / "api" / "status" / uuid ⇒
        tryCatch[UUID, IllegalArgumentException](UUID.fromString(uuid)) map {
          analyzerController.status(_).unsafePerformIO
        } valueOr { err ⇒ BadRequest(jsonErr(err)) }

      case POST -> Root / "api" / "analyze" ⇒
        // TODO: Implement
        ???

      // assets directory
      case req @ GET -> "assets" /: path ⇒
        assetsController.serve(req, path).fold(NotFound())(Task.now)

    })

}
