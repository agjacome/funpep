package funpep.server
package service

import scala.concurrent.ExecutionContext

import scalaz.concurrent._
import scalaz.stream._
import scalaz.syntax.std.option._

import org.http4s._
import org.http4s.dsl._

import funpep.util.functions._

import funpep.server.util.functions._


final class DatasetService private {

  def service(implicit ec: ExecutionContext): HttpService = HttpService {
    case req @ GET -> Root / "fasta" / "bioactive_curated"       ⇒ resource("/fasta/bioactive_curated_peps.faa" , req)
    case req @ GET -> Root / "fasta" / "glycoside_hydrolases"    ⇒ resource("/fasta/glycoside_hydrolases.faa"   , req)
    case req @ GET -> Root / "fasta" / "tetracycline_resistence" ⇒ resource("/fasta/tetracycline_resistance.faa", req)

    case req @ GET -> Root / "ontology" / "biomed" ⇒ resource("/obo/biomed_ontology.obo", req)
    case req @ GET -> Root / "ontology" / "genes"  ⇒ resource("/obo/gene_ontology.obo"  , req)
  }

  // FIXME: performs effect and wraps result in Process, this is wrong
  def resource(file: String, req: Request): Process[Task, Response] =
    StaticFile.fromResource(file, req.some).fold(notFound)(AsyncP(_))

}

object DatasetService {

  def apply(): DatasetService =
    new DatasetService()

  def service(implicit ec: ExecutionContext): HttpService =
    new DatasetService().service

}
