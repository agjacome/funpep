name         := "funpep"
organization := "es.uvigo.ei.sing"
scalaVersion := "2.11.6"
version      := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scalaz"  %% "scalaz-core"       % "7.1.1" ,
  "org.scalaz"  %% "scalaz-effect"     % "7.1.1" ,
  "org.scalaz"  %% "scalaz-concurrent" % "7.1.1" ,

  "org.http4s"  %% "http4s-dsl"         % "0.6.5" ,
  "org.http4s"  %% "http4s-blazeserver" % "0.6.5" ,
  "org.http4s"  %% "http4s-argonaut"    % "0.6.5" ,

  "org.specs2"     %% "specs2-core" % "3.3.1"  % "test",
  "org.scalacheck" %% "scalacheck"  % "1.12.2" % "test"
)

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

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
