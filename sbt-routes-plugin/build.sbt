organization := "com.siderakis"

version := "0.2-SNAPSHOT"

name := "playframework-appengine-routes"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
	"com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
	"com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2",
	"org.specs2" %% "specs2" % "2.3.11" % "test"
)

sbtPlugin := true

publishMavenStyle := true

scalacOptions in Test ++= Seq("-Yrangepos")

publishTo := Some(Resolver.file("Local", Path.userHome / "siderakis.github.com" / "maven" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))