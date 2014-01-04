package controllers

import play.api.mvc._
import play.{Result, Controller}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.google.appengine.api.urlfetch.URLFetchServiceFactory
import java.net.URL
import scala.concurrent.ExecutionContextAE
import ExecutionContextAE.ImplicitsAE._
import scala.concurrent._
import com.google.appengine.api.users.{UserServiceFactory, User}
import play.api.mvc.DiscardingCookie
import play.api.mvc.Cookie

/**
 * User: nick
 * Date: 11/22/13
 */
object PlayController extends Controller {

  def index = Action {
    Ok("Simple Index").as("text")
  }

  def speed(name: String) = Action.async {

    implicit def f2future[T](f: java.util.concurrent.Future[T]) = future(f.get)

    val ws = URLFetchServiceFactory.getURLFetchService

    // http://engineering.linkedin.com/play/play-framework-linkedin
    val start = System.currentTimeMillis()
    def getLatency(r: Any): Long = System.currentTimeMillis() - start
    val googleTime = ws.fetchAsync(new URL("http://www.google.com")).map(getLatency)
    val yahooTime = ws.fetchAsync(new URL("http://www.yahoo.com")).map(getLatency)
    val bingTime = ws.fetchAsync(new URL("http://www.bing.com")).map(getLatency)

    Future.sequence(Seq(googleTime, yahooTime, bingTime)).map {
      case times =>
        Ok(s"<h1>hello $name,</h1> here is some data:" +
          Map("google" -> times(0), "yahoo" -> times(1), "bing" -> times(2)).mapValues(_ + "ms").mkString("<ul><li>", "</li><li>", "</li></ul>")
        )
    }
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

  /**
   * Wrap an existing request. Useful to extend a request.
   */
  class WrappedRequest(request: RequestHeader) extends Request {
    def queryString = request.queryString

    def path = request.path

    def method = request.method

    def req: HttpServletRequest = request.req

    def resp: HttpServletResponse = request.resp
  }

  case class AuthenticatedRequest(user: User, request: RequestHeader) extends WrappedRequest(request)

  object Authenticated {
    def apply(block: AuthenticatedRequest => Result): Action = new Action {
      def apply(ctx: RequestHeader) = {
        // Check authentication
        if (UserServiceFactory.getUserService.isUserAdmin) {
          val user = UserServiceFactory.getUserService.getCurrentUser
          block(AuthenticatedRequest(user, ctx))
        } else {
          NotAcceptable
        }

      }
    }
  }

  def auth = Authenticated {
    request =>
      Ok("hello, " + request.user.getNickname)
  }


}
