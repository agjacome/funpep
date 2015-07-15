name         := "funpep"
organization := "es.uvigo.ei.sing"
scalaVersion := "2.11.7"
version      := "0.0.1-SNAPSHOT"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "org.scalaz"  %% "scalaz-core"       % "7.1.3",
  "org.scalaz"  %% "scalaz-effect"     % "7.1.3",
  "org.scalaz"  %% "scalaz-iteratee"   % "7.1.3",
  "org.scalaz"  %% "scalaz-concurrent" % "7.1.3",

  "org.http4s"  %% "http4s-dsl"         % "0.8.4",
  "org.http4s"  %% "http4s-argonaut"    % "0.8.4",
  "org.http4s"  %% "http4s-blazeserver" % "0.8.4",

  "com.typesafe.scala-logging" %% "scala-logging"   % "3.1.0" ,
  "ch.qos.logback"             %  "logback-classic" % "1.1.3"

  // NO TESTS ATM
  // "org.scalacheck" %% "scalacheck"                % "1.12.4" % "test",
  // "org.specs2"     %% "specs2-core"               % "3.6.1"  % "test",
  // "org.scalaz"     %% "scalaz-scalacheck-binding" % "7.1.2"  % "test" exclude("org.scalacheck", "scalacheck_2.11"),
  // "org.specs2"     %% "specs2-scalacheck"         % "3.6.1"  % "test" exclude("org.scalacheck", "scalacheck_2.11")
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard"
)

scalacOptions in (Compile, console) ~= { _ filterNot Set(
  "-Xfatal-warnings", "-Ywarn-unused-import"
)}

wartremoverWarnings ++= Warts.all

initialCommands in console := """
  |import scala.concurrent.ExecutionContext.Implicits.global
  |
  |import scalaz._
  |import scalaz.Scalaz._
  |
  |import es.uvigo.ei.sing.funpep._
  |import es.uvigo.ei.sing.funpep.data._
  |
  |val config = Funpep.config
""".stripMargin

shellPrompt := { _ ⇒ "funpep » " }
