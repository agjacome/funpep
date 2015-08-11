package funpep
package data

import scalaz.Maybe

trait Compound[Code] {
  def code: Code
  def name: Maybe[String]
  def mass: Maybe[Double]
}
