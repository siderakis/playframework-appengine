import sbtappengine.Plugin.{AppengineKeys => gae}

name := "PlayFramework-AppEngine"

scalaVersion := "2.10.2"

resolvers += "Scala AppEngine Sbt Repo" at "http://siderakis.github.com/maven"

libraryDependencies ++= Seq(
  "com.siderakis" %% "futuraes" % "0.1-SNAPSHOT",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
)

appengineSettings

(gae.onStartHooks in gae.devServer in Compile) += { () =>
  println("hello")
}

(gae.onStopHooks in gae.devServer in Compile) += { () =>
  println("bye")
}

play.PlayProject.defaultPlaySettings
