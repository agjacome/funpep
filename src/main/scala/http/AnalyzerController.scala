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

import data._
import es.uvigo.ei.sing.funpep.util.JsonUtils._


private[http] final case class JobSettings (
  email:       Email,
  threshold:   Double,
  annotations: Map[FastaEntry.ID, String],
  reference:   Fasta,
  comparing:   Fasta
)

private[http] object JobSettings {

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Nothing", "org.brianmckenna.wartremover.warts.Any"))
  implicit val JobSettingsCodecJson: CodecJson[JobSettings] =
    casecodec5(JobSettings.apply, JobSettings.unapply)(
      "email", "threshold", "annotations", "reference", "comparing"
    )

}

final class AnalyzerController (val analyzer: Analyzer) extends LazyLogging {

  def analyze(settings: JobSettings): IO[Task[Response]] = {
    val id  = uuid
    val job = Job(id, Job.Initial, settings.email, settings.threshold, settings.annotations)

    analyzer.analyze(job, settings.comparing, settings.reference).fold(err ⇒ {
      logger.error("Error adding new job to analyzer queue", err)
      InternalServerError(jsonErr(err))
    }, _ ⇒ Ok(Json("uuid" := id.asJson)))
  }

  def queueSize: Task[Response] =
    Ok(Json("queue_size" := analyzer.queueSize))

  def status(id: UUID): IO[Task[Response]] =
    analyzer.status(id).fold(_ ⇒ {
      logger.error(s"Error checking status of job '$id'. It probably does not exist.")
      NotFound(jsonErr(s"Job with id '$id' not found"))
    }, status ⇒ Ok(status.asJson))

}
