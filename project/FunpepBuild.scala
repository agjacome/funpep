import sbt._
import sbt.Keys._

import scala.util.Properties.envOrNone

object FunpepBuild extends Build {

  val apiVersion  = TaskKey[(Int, Int)]("api-version", "Defines the API compatibility version")
  val javaVersion = TaskKey[String]("java-version", "Defines the target JVM version")

  def getApiVersion(version: String): (Int, Int) = {
    val Matcher = """(\d+)\.(\d+)\..*""".r
    version match {
      case Matcher(major, minor) ⇒ (major.toInt, minor.toInt)
    }
  }

}
