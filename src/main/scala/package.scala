package es.uvigo.ei.sing

import java.nio.file.{ Path, Paths }
import java.util.UUID

import scalaz._
import scalaz.Scalaz._


package object funpep {

  val ¶ = System.lineSeparator

  def uuid: UUID = UUID.randomUUID

  def resource(resource: String): java.net.URL =
    Option(Thread.currentThread.getContextClassLoader) err {
      "Context classloader is not set for the current thread."
    } getResource resource

  def property(name: String): Option[String] =
    Option(System.getProperty(name))

  implicit class StringOps(val str: String) extends AnyVal {
    def toPath:  Path = Paths.get(str)
    def uncased: CaseInsensitive[String] = CaseInsensitive(str)
  }

}
