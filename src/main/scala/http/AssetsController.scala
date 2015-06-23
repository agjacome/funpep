package es.uvigo.ei.sing.funpep
package http

import java.io.InputStream
import java.net.URL

import scala.collection.concurrent.TrieMap

import scalaz.Scalaz._
import scalaz.effect.IO

import org.http4s.{ Request, Response, StaticFile }
import org.http4s.dsl.{ Path ⇒ HttpPath }

import com.typesafe.scalalogging.LazyLogging

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

final class AssetsController (val config: Config) extends LazyLogging {

  import Asset._
  import AssetsController._

  private val cache = TrieMap.empty[Name, Option[Asset]]

  // FIXME: STUB. Add ETag based on Asset hash, check If-None-Match, etc...
  def serve(request: Request, assetName: HttpPath): Option[Response] =
    for {
      asset    ← asset(assetName.toString)
      response ← StaticFile.fromURL(asset.url, Some(request))
    } yield response

  def asset(name: Name): Option[Asset] =
    cache.getOrElseUpdate(name, assetURL(name) map {
      Asset(name, _, config.httpDigest ? assetHash(name) | none[Hash])
    })

  // file "hash-name" is the same as "name" (see sbt-digest doc), we have
  // already got the hash if present to be used in the ETag header, so content
  // can be retrieved directly from the non-prepended-hash file
  private def assetURL(path: String): Option[URL] =
    resource(assetsPath + path + (config.httpGzip ? gzipExtension | ""))

  private def assetHash(path: String): Option[Hash] = {
    resourceStream(assetsPath + path + hashExtension) map readFirstLine >>= {
      _.run.unsafePerformIO valueOr { err ⇒
        logger.error(s"Error reading asset $path$hashExtension. No ETag will be issued with this asset.", err)
        none[Hash]
      }
    }
  }

  private val readFirstLine: InputStream ⇒ IOThrowable[Option[String]] =
    _.toReader.point[IO].bracket(_.closeIO)(_.readLineIO).catchLeft

}

object AssetsController {

  import Config.syntax._

  private[http] lazy val hashExtension = ".md5"
  private[http] lazy val gzipExtension = ".gz"

  private[http] lazy val assetsPath = "/assets"

  def apply(config: Config): AssetsController =
    new AssetsController(config)

  def apply: Configured[AssetsController] =
    Configured { new AssetsController(_) }

}
