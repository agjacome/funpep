package funpep.server
package util

import atto.syntax.parser._

import funpep.util.parsers._


private[util] trait Extractors {

  object UUID {
    def unapply(str: String): Option[java.util.UUID] =
      uuid.parseOnly(str).option
  }

}
