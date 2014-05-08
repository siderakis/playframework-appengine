scalaVersion := "2.10.3"

resolvers += "Scala AppEngine Sbt Repo" at "http://siderakis.github.com/maven"

addSbtPlugin("com.siderakis" %% "playframework-appengine-routes" % "0.2-SNAPSHOT")

addSbtPlugin("com.eed3si9n" % "sbt-appengine" % "0.6.0")

// needed because sbt-twirl depends on twirl-compiler which is only available
// at repo.spray.io
resolvers += "spray repo" at "http://repo.spray.io"

addSbtPlugin("io.spray" % "sbt-twirl" % "0.7.0")