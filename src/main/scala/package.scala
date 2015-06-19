package es.uvigo.ei.sing

import java.io.InputStream
import java.net.URL
import java.nio.file.{ Path, Paths }
import java.util.UUID

import scalaz.CaseInsensitive
import scalaz.syntax.std.option._

package object funpep {

  def nl: String = System.lineSeparator

  def uuid: UUID = UUID.randomUUID

  def classLoader: ClassLoader =
    Option(Thread.currentThread.getContextClassLoader) err {
      "Context classloader is not set for the current thread"
    }

  def resource(name: String): URL =
    Option(classLoader.getResource(name)) err {
      s"Resource $name not found in classpath"
    }

  def resourceStream(name: String): InputStream =
    Option(classLoader.getResourceAsStream(name)) err {
      s"Resource $name not found in classpath"
    }

  def property(name: String): Option[String] =
    Option(System.getProperty(name))

  implicit class StringOps(val str: String) extends AnyVal {
    def toPath:  Path = Paths.get(str)
    def uncased: CaseInsensitive[String] = CaseInsensitive(str)
  }

}
