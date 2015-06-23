resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("com.eed3si9n"     % "sbt-assembly"    % "0.13.0")
addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.13"  )

addSbtPlugin("com.typesafe.sbt" % "sbt-web"    % "1.2.1" )
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip"   % "1.0.0" )
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0" )
addSbtPlugin("com.typesafe.sbt" % "sbt-rjs"    % "1.0.7" )
