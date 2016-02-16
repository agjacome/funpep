package funpep.server
package service

import scala.concurrent.ExecutionContext

import org.http4s._
import org.http4s.server._


final class RouterService private (
  val analyzerService: AnalyzerService[_],
  val datasetsService: DatasetService
) {

  def service(middleware: HttpService ⇒ HttpService)(implicit ec: ExecutionContext): HttpService =
    middleware(service)

  def service(implicit ec: ExecutionContext): HttpService =
    Router(
      "/analysis" → analyzerService.service,
      "/datasets" → datasetsService.service
    )

}

object RouterService {

  def apply(
    analyzer: AnalyzerService[_],
    datasets: DatasetService
  ): RouterService =
    new RouterService(analyzer, datasets)

  def service(
    analyzer: AnalyzerService[_],
    datasets: DatasetService
  )(implicit ec: ExecutionContext): HttpService =
    new RouterService(analyzer, datasets).service

  def service(
    middleware: HttpService ⇒ HttpService,
    analyzer:   AnalyzerService[_],
    datasets:   DatasetService
  )(implicit ec: ExecutionContext): HttpService =
    new RouterService(analyzer, datasets).service(middleware)

}
