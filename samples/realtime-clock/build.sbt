import sbtappengine.Plugin.{AppengineKeys => gae}

import play.PlayProject

name := "RealTime-Clock"

scalaVersion := "2.10.2"

resolvers += "Scala AppEngine Sbt Repo" at "http://siderakis.github.com/maven"

resolvers += "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.siderakis" %% "futuraes" % "0.1-SNAPSHOT",
  "com.siderakis" %% "playframework-appengine-mvc" % "0.2-SNAPSHOT",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
  "play" %% "play-iteratees" % "2.1.5",
  "com.siderakis" %% "futuraes" % "0.1-SNAPSHOT"
)

appengineSettings

PlayProject.defaultPlaySettings

Twirl.settings

