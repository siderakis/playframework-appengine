package play

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import play.api.http.Status
import play.api.http.Status._
import play.api.mvc.HttpRequest
import play.api.mvc.HttpRequest
import play.Results
import scala.concurrent._
import scala.concurrent.duration._

import play.api.mvc._

class PlayAppEngineServlet extends HttpServlet with Results {

  import ExecutionContextAE.ImplicitsAE._


  val NotFoundPage = PartialFunction[RequestHeader, Handler] {
    _ => new Handler {
      def apply(ctx: RequestHeader): Result = NotFound
    }
  }

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) = {
    if (!req.getRequestURI.contains("fav")) {
      val result = (Routes.routes orElse NotFoundPage)(HttpRequest(req))(HttpRequest(req))
      resp.setStatus(result.status)
      resp.getOutputStream.println(result.body)
    }
  }
}