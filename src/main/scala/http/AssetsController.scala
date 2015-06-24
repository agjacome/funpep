package es.uvigo.ei.sing.funpep
package http

import java.io.BufferedReader
import java.net.URL

import scala.collection.concurrent.TrieMap

import scalaz.Scalaz._
import scalaz.effect.IO

import org.http4s.{ Request, Response, StaticFile }
import org.http4s.dsl.{ Path ⇒ HttpPath }
import org.http4s.headers.ETag

import data.Config
import es.uvigo.ei.sing.funpep.util.IOUtils._


private[http] final case class Asset (
  name: Asset.Name,
  url:  URL,
  hash: Option[Asset.Hash]
)

private[http] object Asset {
  type Name = String
  type Hash = String
}

final class AssetsController {

  import Asset._
  import AssetsController._

  private val cache = TrieMap.empty[Name, Asset]

  // FIXME: STUB. Check If-None-Match, etc...
  def serve(request: Request, assetName: HttpPath): Option[Response] =
    asset(assetName.toString) >>= {
      asset ⇒ StaticFile.fromURL(asset.url, Some(request)) map {
        response ⇒ asset.hash.fold(response)(h ⇒ response.putHeaders(ETag(h)))
      }
    }

  def asset(name: Name): Option[Asset] =
    cache.get(name).fold({
      val asset = assetURL(name).map(Asset(name, _, assetHash(name)))
      asset.foreach(a ⇒ cache.update(name, a))
      asset
    })(a ⇒ a.some)

  private def assetURL(path: String): Option[URL] =
    resource(assetsRoute + path)

  private def assetHash(path: String): Option[Hash] =
    resourceReader(assetsRoute + path + ".md5") map readFirstLine >>= {
      _.toOption.run.unsafePerformIO.flatten
    }

  private val readFirstLine: BufferedReader ⇒ IOThrowable[Option[String]] =
    _.point[IO].bracket(_.closeIO)(_.readLineIO).catchLeft

}

object AssetsController {
  lazy val assetsRoute = "/assets"
}
