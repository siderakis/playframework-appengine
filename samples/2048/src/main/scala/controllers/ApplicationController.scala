package controllers

import com.google.appengine.api.channel.{ChannelMessage, ChannelServiceFactory}
import scala.util.Random
import scala.collection.mutable
import scala.io.Source

import play.api.mvc._
import play.Controller


//https://developers.google.com/appengine/docs/java/modules/#Java_Background_threads
//https://developers.google.com/appengine/docs/java/channel/
//https://www.playframework.com/documentation/2.2.x/Enumerators
//https://github.com/playframework/playframework/blob/321af079941f64cdd2cf32b407d4026f7e49dfec/framework/src/play/src/main/scala/play/api/mvc/Results.scala

object Application extends Controller {


  val connectedSet = mutable.Set[String]()

  def connected() = Action {
    req =>
      val who = ChannelServiceFactory.getChannelService.parsePresence(req.req)
      val did = if (who.isConnected) "connected" else "disconnected"
      println(s"${who.clientId} just $did")
      if (who.isConnected) {
        connectedSet.add(who.clientId())
      } else {
        connectedSet.remove(who.clientId())
      }
      Ok("")
  }

  def move() = Action {

    req =>
      val body = Source.fromInputStream(req.req.getInputStream).mkString

      val channelService = ChannelServiceFactory.getChannelService

      connectedSet.
        map(new ChannelMessage(_, body)).
        foreach(channelService.sendMessage)

      Ok("")


  }

  def index() = Action {
    val token = ChannelServiceFactory.getChannelService.createChannel(Random.alphanumeric.take(10).mkString)

    Ok(html.index.render(token).toString)
  }

}

