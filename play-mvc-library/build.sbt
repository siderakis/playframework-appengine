sbtPlugin := false

organization := "com.siderakis"

name := "playframework-appengine-mvc"

version := "0.2-SNAPSHOT"

scalaVersion := "2.10.2"

resolvers += "Scala AppEngine Sbt Repo" at "http://siderakis.github.com/maven"

libraryDependencies ++= Seq(
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.specs2" %% "specs2" % "2.3.10" % "test"
)

publishMavenStyle := true

publishTo := Some(Resolver.file("Local", Path.userHome / "siderakis.github.com" / "maven" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))