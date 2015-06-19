name         := "funpep"
organization := "es.uvigo.ei.sing"
scalaVersion := "2.11.6"
version      := "0.0.1-SNAPSHOT"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "org.scalaz"  %% "scalaz-core"       % "7.1.2",
  "org.scalaz"  %% "scalaz-effect"     % "7.1.2",
  "org.scalaz"  %% "scalaz-iteratee"   % "7.1.2",
  "org.scalaz"  %% "scalaz-concurrent" % "7.1.2",

  "org.http4s"  %% "http4s-dsl"         % "0.8.1",
  "org.http4s"  %% "http4s-blazeserver" % "0.8.1",
  "org.http4s"  %% "http4s-argonaut"    % "0.8.1",
  "org.http4s"  %% "http4s-twirl"       % "0.8.1",

  "org.scalacheck" %% "scalacheck"  % "1.12.4" % "test",
  "org.specs2"     %% "specs2-core" % "3.6.1"  % "test",

  "org.scalaz"     %% "scalaz-scalacheck-binding" % "7.1.2" % "test" exclude("org.scalacheck", "scalacheck_2.11"),
  "org.specs2"     %% "specs2-scalacheck"         % "3.6.1" % "test" exclude("org.scalacheck", "scalacheck_2.11")
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
  |import scalaz._
  |import scalaz.Scalaz._
  |import es.uvigo.ei.sing.funpep._
""".stripMargin

shellPrompt := { _ ⇒ "funpep » " }

lazy val funpep = (project in file(".")).enablePlugins(SbtTwirl)
