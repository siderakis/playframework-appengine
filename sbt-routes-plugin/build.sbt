organization := "com.siderakis"

name := "playframework-appengine-routes"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
	"com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
	"com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"	
)

sbtPlugin := true

publishMavenStyle := true

publishTo := Some(Resolver.file("Local", Path.userHome / "siderakis.github.com" / "maven" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))