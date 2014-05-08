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

  case class Channel(userId: Long, connected: mutable.Seq[Boolean]){


  }

  trait ChannelManager {

    def getFreeClient(user: String): String

    def onPresentsChange(clientId: String, connected: Boolean): Unit

    def sendMessage(user: String, message: String): Unit


  }

  object ChannelManagerMemory extends ChannelManager{
    def getFreeClient(user: String): String = ???

    def onPresentsChange(clientId: String, connected: Boolean): Unit = ???

    def sendMessage(user: String, message: String):
  }

  /**
   * A String Enumerator producing a formatted Time message every 100 millis.
   * A callback enumerator is pure an can be applied on several Iteratee.
   */
  lazy val clock: Enumerator[String] = {

    import java.util._
    import java.text._
    import scala.concurrent.Promise
    val dateFormat = new SimpleDateFormat("HH mm ss")

    Enumerator.generateM[String] {
      println("CLOCK TICK")
      // Promise.timeout(Some(dateFormat.format(new Date)), 1000 milliseconds)

      val p = Promise[Option[String]]()
      Thread.sleep(1000)
      p.complete(Try(if (stillConnected("foo")) Some(dateFormat.format(new Date)) else None))
      p.future
    }
  }

  def stillConnected(name: String) = present.contains(name)

  def connected() = Action {
    req =>
      val who = ChannelServiceFactory.getChannelService.parsePresence(req.req)
      val did = if (who.isConnected) "connected" else "disconnected"
      println(s"${who.clientId} just $did")
      if (who.isConnected) {
        // store connected state for each token?
        doBackgroundStuff(who.clientId())
      } else {
        present.update("foo", present("foo") - token)
      }
      Ok("")
  }

  def doBackgroundStuff(token: String) {
    val thread = ThreadManager.createBackgroundThread(new Runnable() {
      def run() {
        println("BACKGROUND THREAD")

        // TODO terminate on channel close event
        val send = Iteratee.foreach[String] {
          data =>
            println("SENDING MESSAGE")
            ChannelServiceFactory.getChannelService.sendMessage(new ChannelMessage(token, data))
        }

        clock &> Enumeratee.take[String](60) run send //&> takeWhile(present.contains("foo")) run send


      }
    })
    thread.start()

  }

  val present = mutable.Map[String, Seq[String]]().withDefaultValue(Seq())

  def index() = Action {
    //Ok(views.html.index())
    val token = ChannelServiceFactory.getChannelService.createChannel("foo")

    present.update("foo", present("foo") ++ Seq(token))

    Ok(html.index.render(token).toString)
  }

  //  def takeWhile[E](f: => Boolean): iteratee.Enumeratee[E, E] = new CheckDone[E, E] {
  //
  //    def step[A](f: => Boolean)(k: K[E, A]): K[E, Iteratee[E, A]] = {
  //      case in@(Input.El(_) | Input.Empty) if f => new Enumeratee.CheckDone[E, E] {
  //        def continue[A](k: K[E, A]) = Cont(step(f)(k))
  //      } &> k(in)
  //      case Input.EOF => Done(Cont(k), Input.EOF)
  //    }
  //
  //    def continue[A](k: K[E, A]) = Cont(step(f)(k))
  //
  //  }

  def liveClock = Action {
    //Ok.chunked(clock &> Comet(callback = "parent.clockChanged"))
    Ok("TODO")
  }

}

