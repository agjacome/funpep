package es.uvigo.ei.sing

import java.io.{ BufferedReader, InputStream }
import java.net.URL
import java.nio.file.{ Path, Paths }
import java.util.UUID

import scalaz.CaseInsensitive

package object funpep {

  def nl: String = System.lineSeparator

  def uuid: UUID = UUID.randomUUID

  def resource(name: String): Option[URL] =
    Option(getClass.getResource(name))

  def resourceStream(name: String): Option[InputStream] =
    Option(getClass.getResourceAsStream(name))

  def property(name: String): Option[String] =
    Option(System.getProperty(name))

  implicit class StringOps(val str: String) extends AnyVal {
    def toPath:  Path = Paths.get(str)
    def uncased: CaseInsensitive[String] = CaseInsensitive(str)
  }

}
