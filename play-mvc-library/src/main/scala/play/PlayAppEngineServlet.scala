package play

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet, Cookie}

import play.api.mvc._
import play.core.Router.Routes

class PlayAppEngineServlet extends HttpServlet with Results {

  val NotFoundPage = PartialFunction[RequestHeader, Handler] {
    _ => new Handler {
      def apply(ctx: RequestHeader): Result = NotFound
    }
  }

  private[this] val generatedRoute: PartialFunction[RequestHeader, Handler] = {
    //TODO: add error handling
    val name = "play.Routes"
    val router = Class.forName(name + "$").getField("MODULE$").get(null).asInstanceOf[Routes]
    router.routes
  }

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) = {
    val request = HttpRequest(req, resp)
    val handler = (generatedRoute orElse NotFoundPage)(request)
    val result = handler(request)
    resp.setStatus(result.status)
    result.cookies.map(c => new Cookie(c.name, c.value)).foreach(resp.addCookie)
    resp.getOutputStream.println(result.body)
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) = doGet(req, resp)

}