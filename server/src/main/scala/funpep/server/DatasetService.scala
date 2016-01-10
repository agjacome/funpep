package funpep.server

import scala.concurrent.ExecutionContext

import scalaz.concurrent._
import scalaz.syntax.std.option._

import org.http4s._
import org.http4s.dsl._
import org.http4s.server._

import funpep.util.functions._

import util.functions._


final class DatasetService private {

  def service(implicit ec: ExecutionContext): HttpService = HttpService {

    case req @ GET -> Root / "bioactive_curated"       ⇒ resource("bioactive_curated_peps.faa" , req)
    case req @ GET -> Root / "glycoside_hydrolases"    ⇒ resource("glycoside_hydrolases.faa"   , req)
    case req @ GET -> Root / "tetracycline_resistence" ⇒ resource("tetracycline_resistance.faa", req)

    case req @ GET -> Root / "ontology" ⇒ resource("biomed_ontology.obo", req)

    case _ ⇒ MethodNotAllowed()

  }

  // FIXME: performs effect and wraps result in Process, this is wrong
  def resource(file: String, req: Request): Task[Response] =
    StaticFile.fromResource(file, req.some).fold(notFound)(AsyncP(_))

}

object DatasetService {

  def apply[A](implicit ec: ExecutionContext): HttpService =
    new DatasetService().service

}
