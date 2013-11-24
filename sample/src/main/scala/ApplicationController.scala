package controllers

import play.api.mvc.{Result, Action}
import play.Controller

/**
 * User: nick
 * Date: 11/22/13
 */
object PlayController extends Controller {

  def index = Action {
    Ok("Simple Index")
  }

  def hello(name: String) = Action {
    Ok("Hello " + name)
  }

  def hello2(name: String) = Action {
    Accepted("Hello mello " + name)
  }


}
