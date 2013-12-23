package controllers

import play.api.mvc.{DiscardingCookie, Cookie, Action}
import play.Controller
import javax.servlet.http.HttpServletRequest

/**
 * User: nick
 * Date: 11/22/13
 */
object PlayController extends Controller {

  def index = Action {
    Ok("Simple Index").as("text")
  }

  def hello(name: String) = Action {
    Ok(s"Hello $name")
  }

  def meow(name: String) = Action {
    request =>

    //can access HttpServletRequest and HttpServletResponse directly
      val servletReq: HttpServletRequest = request.req
      val contentType = servletReq.getContentType

      if (name == "kitty")
        Accepted(s"$name says meow").
          withCookies(new Cookie("i_can_haz", "cookies")).
          withHeaders(CONTENT_TYPE -> contentType)
      else
        NotAcceptable(s"sorry $name, <b>kitties</b> only").
          withHeaders("do_try" -> "kitty").
          discardingCookies(DiscardingCookie("i_can_haz")).
          as("text/html")

  }


}
