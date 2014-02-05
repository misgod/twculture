name := "server"

version := "1.0"


resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies += "io.spray" %%  "spray-json" % "1.2.5"


libraryDependencies += "com.google.code.gson" % "gson" % "2.2.4"



libraryDependencies += "org.specs2" %% "specs2" % "2.3.7" % "test"

scalacOptions in Test ++= Seq("-Yrangepos")

  // Read here for optional dependencies:
  // http://etorreborre.github.io/specs2/guide/org.specs2.guide.Runners.html#Dependencies

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)
