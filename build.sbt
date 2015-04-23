name         := "funpep"
organization := "es.uvigo.ei.sing"
scalaVersion := "2.11.6"
version      := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scalaz"  %% "scalaz-core"       % "7.1.1" ,
  "org.scalaz"  %% "scalaz-effect"     % "7.1.1" ,
  "org.scalaz"  %% "scalaz-iteratee"   % "7.1.1" ,
  "org.scalaz"  %% "scalaz-concurrent" % "7.1.1" ,

  "org.http4s"  %% "http4s-dsl"         % "0.6.5" ,
  "org.http4s"  %% "http4s-blazeserver" % "0.6.5" ,
  "org.http4s"  %% "http4s-argonaut"    % "0.6.5" ,

  "org.scalacheck" %% "scalacheck"  % "1.11.6" % "test" ,
  "org.specs2"     %% "specs2-core" % "3.4"    % "test" ,

  "org.scalaz"     %% "scalaz-scalacheck-binding" % "7.1.1"  % "test" exclude("org.scalacheck", "scalacheck_2.11") ,
  "org.specs2"     %% "specs2-scalacheck"         % "3.4"    % "test" exclude("org.scalacheck", "scalacheck_2.11")
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

// Faulty warts in some cases, will use "Warts.all" and explicitly suppress
// warnings in those once WartRemover 0.13 is published with it obeying
// the @SuppressWarnings annotation.
wartremoverWarnings ++= Warts.allBut(
  Wart.Any, Wart.NoNeedForMonad, Wart.Nothing
)

initialCommands in console := """
  |import scalaz._
  |import scalaz.Scalaz._
  |import es.uvigo.ei.sing.funpep._
""".stripMargin

shellPrompt := { _ ⇒ "funpep » " }
