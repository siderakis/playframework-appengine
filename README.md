playframework-appengine
=======================

Adapting the Play Framework's Core/MVC/Routing to work on Google App Engine


##Status

1. Route Plugin (done)
2. MVC, Action, Request, Response API ported to Servlets 2.5 (basics done, aysnc action in-progress)
3. Reverse Routing (not started but planned)

Scala 2.10
Sbt 0.13
Play 2.3

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
	
	
### Update web.xml

	<servlet>
		<servlet-name>Play</servlet-name>
		<servlet-class>play.PlayAppEngineServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>Play</servlet-name>
		<url-pattern>/*</url-pattern>
	</servlet-mapping>


## Implementation Details

I decided not to include `BodyParsers` and `Action[A]` has been simplified to `Action` that operates on String content only.  I may inlcude BodyParsers later depending on how many dependencies there are.

This port should work in any servlets envoirment, not just App Engine.

#### Also check out these related scala projects that work great on App Engine:  

[Twirl](https://github.com/spray/twirl) The Play framework Scala **template engine**, stand-alone and packaged as an SBT plugin

[Play-Json-Standalone](https://github.com/mandubian/play-json-alone) Plays amazing JSON API, as a stand-alone library.
 
	
