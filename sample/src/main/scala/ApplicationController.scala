package controllers

import play.api.mvc._
import play.Controller
import javax.servlet.http.HttpServletRequest
import scala.concurrent._
import play.api.mvc.DiscardingCookie
import play.api.mvc.Cookie
import java.util.concurrent.Executor
import scala.concurrent.impl.{Futures, ExecutionContextAppEngineImpl2}

/**
 * User: nick
 * Date: 11/22/13
 */
object PlayController extends Controller {

  def index = Action {
    implicit request =>
      Ok( s"""<a href="${new ReversePlayController().speed().absoluteURL()}">speed</a>""").as("text")
  }


  def speed(name: String) = Action {

    implicit def f2future[T](f: java.util.concurrent.Future[T]) = future(f.get)

    println("Thread (0) == " + Thread.currentThread().getName)

    implicit lazy val z: ExecutionContextAppEngineImpl2 = new ExecutionContextAppEngineImpl2(null: Executor, (Throwable) => {})

    Futures {
      Thread.sleep(2000)
      println("Thread (1) == " + Thread.currentThread().getName)

      Futures{
        Thread.sleep(2000)
        println("Thread (2) == " + Thread.currentThread().getName)

      }

      "hello"
    }.map(_ + " world! *****").foreach(println)



    z.block()


    Ok(s"<h1>hello $name,</h1> no data for you.")

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

  //  /**
  //   * Wrap an existing request. Useful to extend a request.
  //   */
  //  class WrappedRequest(request: RequestHeader) extends Request {
  //    def queryString = request.queryString
  //
  //    def path = request.path
  //
  //    def method = request.method
  //
  //    def req: HttpServletRequest = request.req
  //
  //    def resp: HttpServletResponse = request.resp
  //  }
  //
  //  case class AuthenticatedRequest(user: User, request: RequestHeader) extends WrappedRequest(request)
  //
  //  object Authenticated {
  //    def apply(block: AuthenticatedRequest => Result): Action = new Action {
  //      def apply(ctx: RequestHeader) = {
  //        // Check authentication
  //        if (UserServiceFactory.getUserService.isUserAdmin) {
  //          val user = UserServiceFactory.getUserService.getCurrentUser
  //          block(AuthenticatedRequest(user, ctx))
  //        } else {
  //          NotAcceptable
  //        }
  //
  //      }
  //    }
  //  }

  def auth = Action {
    request =>
      Ok("hello, ") // + request.user.getNickname)
  }


}
