playframework-appengine
=======================

Adapting the Play Framework's Core/MVC/Routing to work on Google App Engine


## Getting started 

### Adding dependencies

In `build.sbt`:

	resolvers += "Scala AppEngine Sbt Repo" at "http://siderakis.github.com/maven"

	import play.PlayProject

	libraryDependencies ++= Seq(
	  "com.siderakis" %% "futuraes" % "0.1-SNAPSHOT",
	  "com.siderakis" %% "playframework-appengine-mvc" % "0.1-SNAPSHOT",
	  "javax.servlet" % "servlet-api" % "2.5" % "provided",
	  "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
	)

	appengineSettings

	PlayProject.defaultPlaySettings


In `project/plugins.sbt`:
	
	resolvers += "Scala AppEngine Sbt Repo" at "http://siderakis.github.com/maven"
	
	addSbtPlugin("com.siderakis" %% "playframework-appengine-routes" % "0.1-SNAPSHOT")
	
	addSbtPlugin("com.eed3si9n" % "sbt-appengine" % "0.6.0")


### Controller


	object SimpleController extends Controller {
	
	  def index = Action {
	    Ok("So Simple!")
	  }
	
	  def hello(name: String) = Action {
	    Ok("Hello " + name)
	  }
	  
	}


### Write Routes file

	GET 	/		   					controllers.SimpleController.index
	GET 	/hello/:name				controllers.SimpleController.hello(name: String)
	
	

	
