resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("com.eed3si9n"     % "sbt-assembly"    % "0.13.0")
addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.13"  )

addSbtPlugin("com.typesafe.sbt" % "sbt-web"    % "1.2.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

// Required by sbt-web in its pipeline (or warning when packaging/running)
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.7"
