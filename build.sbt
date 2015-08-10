version      in ThisBuild := "0.1.0-SNAPSHOT"
apiVersion   in ThisBuild := getApiVersion(version.value)
scalaVersion in ThisBuild := "2.11.7"
javaVersion  in ThisBuild := "1.8"

lazy val root = Project("funpep", file(".")).settings(common, metadata).settings(
  description := "Functional enrichment of peptide data sets"
).aggregate(core, server)

lazy val core = module("core").settings(
  description := "Core funpep library",

  resolvers ++= Seq(
    "Scalaz Bintray Repo"   at "http://dl.bintray.com/scalaz/releases",
    "tpolecat Bintray Repo" at "http://dl.bintray.com/tpolecat/maven"
  ),

  libraryDependencies ++= Seq(
    "org.scalaz"  %% "scalaz-core"       % "7.1.3",
    "org.scalaz"  %% "scalaz-effect"     % "7.1.3",
    "org.scalaz"  %% "scalaz-iteratee"   % "7.1.3",
    "org.scalaz"  %% "scalaz-concurrent" % "7.1.3",

    "org.scalaz.stream" %% "scalaz-stream" % "0.7.2a",

    "org.tpolecat" %% "atto-core" % "0.4.1"
  )
)

lazy val server = module("server").settings(
  description := "HTTP server providing a REST API to access funpep",

  resolvers += "Oncue Bintray Repo" at "http://dl.bintray.com/oncue/releases",

  libraryDependencies ++= Seq(
    "io.argonaut" %% "argonaut" % "6.0.4" exclude("org.scalaz", "scalaz-core_2.11"),

    "org.http4s"  %% "http4s-dsl"         % "0.8.4",
    "org.http4s"  %% "http4s-argonaut"    % "0.8.4" exclude("io.argonaut", "argonaut_2.11"),
    "org.http4s"  %% "http4s-blazeserver" % "0.8.4",

    "oncue.journal" %% "core" % "2.2.1"
  )
).dependsOn(core % "compile;test->test")

lazy val common = Seq(

  // for jvm 1.8 optimizations
  // libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.5.0",

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
    "-Ywarn-value-discard",
    s"-target:jvm-${javaVersion.value}"

    // scala 2.11.7 ↔ jvm 1.8 optimizations (requires scala-java8-compat)
    // disabled because it is causing some troubles at the moment, will reenable
    // in a future and try to actually solve them
    // "-Ybackend:GenBCode",
    // "-Ydelambdafy:method",
    // "-Yopt:l:classpath"
  ),

  javacOptions ++= Seq(
    "-source", javaVersion.value,
    "-target", javaVersion.value,
    "-Xlint:deprecation",
    "-Xlint:unchecked"
  ),

  scalacOptions in (Compile, console) ~= { _ filterNot Set(
    "-Xfatal-warnings", "-Ywarn-unused-import"
  )},

  wartremoverWarnings ++= Warts.all,

  shellPrompt := { _ ⇒ s"${name.value} » " }
)

lazy val metadata = Seq(
  organization := "es.uvigo.ei.sing",
  developers := List(
    Developer("agjacome", "Alberto G. Jácome"  , "agjacome@esei.uvigo.es", url("https://github.com/agjacome")),
    Developer("aitorbm" , "Aitor Blanco Míguez", "aitorbm@esei.uvigo.es" , url("https://github.com/aitorbm" ))
  ),
  startYear := Some(2015),
  homepage  := Some(url("http://sing.ei.uvigo.es/funpep")),
  licenses  := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.html")),
  scmInfo   := Some(ScmInfo(
    url("https://github.com/agjacome/funpep"),
    "scm:git:https://github.com/agjacome/funpep",
    Some("scm:git:git@github.com/agjacome/funpep")
  ))
)

def module(name: String): Project =
  Project(s"funpep-$name", file(name)).settings(common, metadata)
