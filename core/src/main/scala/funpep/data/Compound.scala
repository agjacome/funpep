package funpep
package data

import scalaz.Maybe


trait Compound {
  def code: Char
  def name: Maybe[String]
  def mass: Maybe[Double]
}
