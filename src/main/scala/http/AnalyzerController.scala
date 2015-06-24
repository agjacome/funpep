package es.uvigo.ei.sing.funpep
package http

import java.util.UUID

import scalaz.concurrent.Task
import scalaz.effect.IO

import argonaut._
import argonaut.Argonaut._

import org.http4s._
import org.http4s.argonaut._
import org.http4s.dsl._

import com.typesafe.scalalogging.LazyLogging

import es.uvigo.ei.sing.funpep.util.JsonUtils._


final class AnalyzerController (val analyzer: Analyzer) extends LazyLogging {

  def analyze(request: Request): Task[Response] =
    ???

  def queueSize: Task[Response] =
    Ok(Json("queue_size" := analyzer.queueSize))

  def status(id: UUID): IO[Task[Response]] =
    analyzer.status(id).map(status ⇒ Ok(status.asJson)) valueOr { _ ⇒
      logger.error(s"Error checking status of job '$id'. It probably does not exist.")
      NotFound(jsonErr(s"Job with id '$id' not found"))
    }

}
