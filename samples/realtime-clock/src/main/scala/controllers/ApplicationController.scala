package controllers

import play.api._
import play.api.mvc._
import com.google.appengine.api.channel.{ChannelFailureException, ChannelMessage, ChannelServiceFactory}
import java.util.Date
import twirl.api.Html
import com.google.appengine.api.ThreadManager
import play.api.libs.iteratee
import scala.util.{Failure, Success, Try}

//import play.api.libs.Comet

import play.api.libs.iteratee._

//import play.api.libs.concurrent._
//import play.{Result, Controller}

import play.api.mvc._
import play.{Result, Controller}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.google.appengine.api.urlfetch.URLFetchServiceFactory
import java.net.URL
import scala.concurrent.ExecutionContextAE
import ExecutionContextAE.ImplicitsAE._
import scala.concurrent._
import play.api.mvc.DiscardingCookie
import play.api.mvc.Cookie
import scala.concurrent.duration._

//import play.api.libs.concurrent.Execution.Implicits.defaultContext

import ExecutionContextAE.ImplicitsAE._


//https://developers.google.com/appengine/docs/java/modules/#Java_Background_threads
//https://developers.google.com/appengine/docs/java/channel/
//https://www.playframework.com/documentation/2.2.x/Enumerators
//https://github.com/playframework/playframework/blob/321af079941f64cdd2cf32b407d4026f7e49dfec/framework/src/play/src/main/scala/play/api/mvc/Results.scala

object Application extends Controller {

  /**
   * A String Enumerator producing a formatted Time message every 100 millis.
   * A callback enumerator is pure an can be applied on several Iteratee.
   */
  lazy val clock: Enumerator[String] = {

    import java.util._
    import java.text._

    val dateFormat = new SimpleDateFormat("HH mm ss")

    Enumerator.generateM {
      println("CLOCK TICK")
      Promise.timeout(Some(dateFormat.format(new Date)), 1000 milliseconds)
    }
  }

  def index() = Action {
    //Ok(views.html.index())
    val token = ChannelServiceFactory.getChannelService.createChannel("foo")

    val thread = ThreadManager.createBackgroundThread(new Runnable() {
      def run {
        println("BACKGROUND THREAD")

        // TODO terminate on channel close event
        val send = Iteratee.foreach[String] {
          data =>
            println("SENDING MESSAGE")
            try {
              ChannelServiceFactory.getChannelService.sendMessage(new ChannelMessage(token, data))
              println("SENT OK")
            } catch {
              case e: ChannelFailureException => println("FAILED")
            }
        }

        clock &> Enumeratee.take[String](60) run send


      }
    })
    thread.start()

    Ok(html.index.render(token).toString)
  }

  def liveClock = Action {
    //Ok.chunked(clock &> Comet(callback = "parent.clockChanged"))
    Ok("TODO")
  }

}

