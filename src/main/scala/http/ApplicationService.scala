package es.uvigo.ei.sing.funpep
package http

import java.lang.{ IllegalArgumentException ⇒ IllegalArg }
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
  val assetsCtrl:   AssetsController,
  val analyzerCtrl: AnalyzerController
) extends LazyLogging {

  // Right-associative path extractor, already in http4s but not published
  private object /: {
    def unapply(path: Path): Option[(String, Path)] =
      unapply(path.toList)
    def unapply(list: List[String]): Option[(String, Path)] =
      list.headOption map { (_, Path(list.drop(1))) }
  }

  val logRequest: PartialFunction[Request, Request] = PartialFunction {
    request ⇒
      logger.info(s"${request.remoteAddr.getOrElse("noaddr")} ⇒ ${request.method}: ${request.uri.path} ${request.uri.query}")
      request
  }

  def router: HttpService =
    HttpService(logRequest andThen {

      case GET -> Root / "api" / "queue_size" ⇒
        analyzerCtrl.queueSize

      case GET -> Root / "api" / "status" / uuid ⇒
        tryCatch[UUID, IllegalArg](UUID.fromString(uuid)).fold(
          err ⇒ BadRequest(jsonErr(err)),
          id  ⇒ analyzerCtrl.status(id).unsafePerformIO()
        )

      case request @ POST -> Root / "api" / "analyze" ⇒
        request.decodeWith(jsonOf[JobSettings]) {
          settings ⇒ analyzerCtrl.analyze(settings).unsafePerformIO()
        }

      case request @ GET -> Root                 ⇒ assetsCtrl.serve(request, "/html/index.html")
      case request @ GET -> Root / "analyze"     ⇒ assetsCtrl.serve(request, "/html/analyze.html")
      case request @ GET -> Root / "check"       ⇒ assetsCtrl.serve(request, "/html/check.html")
      case request @ GET -> Root / "help"        ⇒ assetsCtrl.serve(request, "/html/help.html")
      case request @ GET -> Root / "about"       ⇒ assetsCtrl.serve(request, "/html/about.html")
      case request @ GET -> Root / "robots.txt"  ⇒ assetsCtrl.serve(request, "/html/robots.txt")
      case request @ GET -> Root / "humans.txt"  ⇒ assetsCtrl.serve(request, "/html/humans.txt")
      case request @ GET -> Root / "favicon.ico" ⇒ assetsCtrl.serve(request, "/img/favicon.ico")

      case request @ GET -> "assets" /: "lib" /: path ⇒
        assetsCtrl.serve(request, "/lib" + path.toString)

      case request @ GET -> "assets" /: "css" /: path ⇒
        assetsCtrl.serve(request, "/css" + path.toString)

      case request @ GET -> "assets" /: "js" /: path ⇒
        assetsCtrl.serve(request, "/js" + path.toString)

      case request @ GET -> "assets" /: "img" /: path ⇒
        assetsCtrl.serve(request, "/img" + path.toString)

      case _ ⇒ NotFound()

    })

}
