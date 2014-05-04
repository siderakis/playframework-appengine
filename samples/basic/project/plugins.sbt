scalaVersion := "2.10.3"

resolvers += "Scala AppEngine Sbt Repo" at "http://siderakis.github.com/maven"

addSbtPlugin("com.siderakis" %% "playframework-appengine-routes" % "0.2-SNAPSHOT")

addSbtPlugin("com.eed3si9n" % "sbt-appengine" % "0.6.0")
