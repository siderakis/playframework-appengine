package controllers

import play.api._
import play.api.mvc._
import com.google.appengine.api.channel.{ChannelFailureException, ChannelMessage, ChannelServiceFactory}
import java.util.Date
import twirl.api.Html
import com.google.appengine.api.ThreadManager
import play.api.libs.iteratee
import scala.util.{Failure, Success, Try}
import scala.Some
import scala.Some
import play.api.libs.iteratee.Enumeratee.CheckDone
import scala.collection.mutable

//import play.api.libs.Comet

import play.api.libs.iteratee._

//import play.api.libs.concurrent._
//import play.{Result, Controller}

import play.api.mvc._
import play.{Result, Controller}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.google.appengine.api.urlfetch.URLFetchServiceFactory
import java.net.URL
import scala.concurrent._

// import ExecutionContextAE.ImplicitsAE._

import play.api.mvc.DiscardingCookie
import play.api.mvc.Cookie
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration

//import play.api.libs.concurrent.Execution.Implicits.defaultContext

import ExecutionContextAE.ImplicitsAE._


//https://developers.google.com/appengine/docs/java/modules/#Java_Background_threads
//https://developers.google.com/appengine/docs/java/channel/
//https://www.playframework.com/documentation/2.2.x/Enumerators
//https://github.com/playframework/playframework/blob/321af079941f64cdd2cf32b407d4026f7e49dfec/framework/src/play/src/main/scala/play/api/mvc/Results.scala

object Application extends Controller {



  def connected() = Action {
    req =>
      val who = ChannelServiceFactory.getChannelService.parsePresence(req.req)
      val did = if (who.isConnected) "connected" else "disconnected"
      println(s"${who.clientId} just $did")
      if (who.isConnected) {
        // store connected state for each token?
       // doBackgroundStuff(who.clientId())
      } else {
//        present.update("foo", present("foo") - token)
      }
      Ok("")
  }

  def index() = Action {
    val token = ChannelServiceFactory.getChannelService.createChannel("foo")

    Ok(html.index.render(token).toString)
  }

}

