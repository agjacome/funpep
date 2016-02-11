lazy val root = Project("funpep", file("."))
  .settings(common, metadata)
  .settings(description := "Functional enrichment of peptide data sets")
  .enablePlugins(GitVersioning, GitBranchPrompt)
  .aggregate(core, server)

lazy val core = module("core").settings(
  description := "Core funpep library",

  libraryDependencies ++= List(
    "org.scalaz" %% "scalaz-core"       % "7.1.5",
    "org.scalaz" %% "scalaz-concurrent" % "7.1.5",

    "org.scalaz.stream" %% "scalaz-stream" % "0.7.2a",

    "org.tpolecat" %% "atto-core"   % "0.4.2",
    "org.tpolecat" %% "atto-stream" % "0.4.2",

    "commons-io"             % "commons-io" % "2.4",
    "org.biojava.thirdparty" % "forester"   % "1.005"
  )
)

lazy val server = module("server").dependsOn(core).settings(
  description := "HTTP server providing a REST API over funpep core library",

  libraryDependencies ++= List(
    "io.argonaut" %% "argonaut" % "6.1",

    "org.http4s"  %% "http4s-dsl"          % "0.11.3",
    "org.http4s"  %% "http4s-argonaut"     % "0.11.3",
    "org.http4s"  %% "http4s-blaze-server" % "0.11.3",

    "me.lessis"     %% "courier"                    % "0.1.3",
    "oncue.journal" %% "core"                       % "2.2.1",
    "net.bmjames"   %% "scala-optparse-applicative" % "0.3"
  ),

  mainClass in assembly := Option("funpep.server.FunpepServer")
)

lazy val common = List(

  scalaVersion := "2.11.7",
  javaVersion  := "1.8",

  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1"),

  scalacOptions ++= List(
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
    "-Ywarn-value-discard",
    s"-target:jvm-${javaVersion.value}"
  ),

  javacOptions ++= List(
    "-source", javaVersion.value,
    "-target", javaVersion.value,
    "-Xlint:deprecation",
    "-Xlint:unchecked"
  ),

  wartremoverWarnings ++= Warts.allBut(
    Wart.Nothing,               // Because scalaz-stream loves Nothing
    Wart.ExplicitImplicitTypes, // [wartremover#182](https://github.com/puffnfresh/wartremover/issues/182)
    Wart.Throw                  // [wartremover#188](https://github.com/puffnfresh/wartremover/issues/188)
  ),

  scalacOptions in (Compile, console) ~= { _ filterNot Set(
    "-Xfatal-warnings", "-Ywarn-unused-import"
  )}
)

lazy val metadata = List(
  organization := "es.uvigo.ei.sing",
  developers   := Developer("agjacome", "Alberto G. Jácome", "agjacome@esei.uvigo.es", url("https://github.com/agjacome")) :: Nil,
  startYear    := Option(2015),
  homepage     := Option(url("http://sing.ei.uvigo.es/funpep")),
  licenses     := "MIT" -> url("http://www.opensource.org/licenses/mit-license.html") :: Nil,
  scmInfo      := Option(ScmInfo(
    url("https://github.com/agjacome/funpep"),
    "scm:git:https://github.com/agjacome/funpep",
    Option("scm:git:git@github.com/agjacome/funpep")
  ))
)

def module(name: String): Project =
  Project(s"funpep-$name", file(name)).settings(common, metadata)

def javaVersion = TaskKey[String]("java-version", "Defines the target JVM version")
