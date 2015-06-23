package es.uvigo.ei.sing.funpep
package http

final class AnalyzerController (val analyzer: Analyzer) {

  // TODO: implement

}

object AnalyzerController {

  def apply(analyzer: Analyzer): AnalyzerController =
    new AnalyzerController(analyzer)

}
