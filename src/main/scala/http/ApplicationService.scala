package es.uvigo.ei.sing.funpep
package http

import scala.concurrent.ExecutionContext

import org.http4s.dsl._
import org.http4s.server._

import scalaz.concurrent.Task

object ApplicationService {

  // TODO: Stub
  def service(implicit ec: ExecutionContext): HttpService = HttpService {
    case _ ⇒ MethodNotAllowed()
  }

}

