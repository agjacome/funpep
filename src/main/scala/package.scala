package es.uvigo.ei.sing

import java.nio.file.{ Path, Paths }
import java.util.UUID

import scalaz.CaseInsensitive

package object funpep {

  def nl: String = System.lineSeparator

  def uuid: UUID = UUID.randomUUID

  implicit class StringOps(val str: String) extends AnyVal {
    def toPath:  Path = Paths.get(str)
    def uncased: CaseInsensitive[String] = CaseInsensitive(str)
  }

}
