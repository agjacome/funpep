package funpep
package util

import java.nio.file.{ Path, Paths }


private[util] trait Functions {

  def devnull: Path = Paths.get("/dev/null")

  def discard[A]: A ⇒ Unit = _ ⇒ {}

}
