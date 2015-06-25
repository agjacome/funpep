libraryDependencies += "org.webjars" % "foundation" % "5.5.2"

enablePlugins(SbtWeb)
pipelineStages := Seq(digest)

LessKeys.compress in Assets := true

WebKeys.packagePrefix in Assets := "assets/"
(managedClasspath in Runtime) += (packageBin in Assets).value
