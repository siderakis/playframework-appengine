sbtPlugin := false

organization := "com.siderakis"

name := "playframework-appengine-mvc"

scalaVersion := "2.10.2"

resolvers += "Scala AppEngine Sbt Repo" at "http://siderakis.github.com/maven"

libraryDependencies ++= Seq(
  "javax.servlet" % "servlet-api" % "2.5" % "provided"
)

publishMavenStyle := true

publishTo := Some(Resolver.file("Local", Path.userHome / "siderakis.github.com" / "maven" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))