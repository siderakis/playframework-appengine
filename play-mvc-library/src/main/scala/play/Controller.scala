package play

import play.api.mvc._

import play.api.http.{HeaderNames, Status}
import play.api.http.HeaderNames._

import play.api.mvc.DiscardingCookie
import play.api.mvc.Cookie
import scala.collection.mutable


/**
 * Defines utility methods to generate `Action` and `Results` types.
 *
 * For example:
 * {{{
 * object Application extends Controller {
 *
 *   def hello(name:String) = Action { request =>
 *     Ok("Hello " + name)
 *   }
 *
 * }
 * }}}
 */
trait Controller extends Results with Status with HeaderNames {


}

case class ResponseExtras(cookies: mutable.Map[String, String] = mutable.Map())


object Controller extends Controller

/** Helper utilities to generate results. */
object Results extends Results {

  /** Empty result, i.e. nothing to send. */
  case class EmptyContent()

}

trait Response {

  /**
   * Handles a result.
   *
   * Depending on the result type, it will be sent synchronously or asynchronously.
   */
  def handle(result: Result): Unit

}

/** Helper utilities to generate results. */
trait Results {

  import play.api.http.Status._
  import play.api.http.HeaderNames._


  /**
   * Generates default `SimpleResult` from a content type, headers and content.
   *
   * @param status the HTTP response status, e.g ‘200 OK’
   */
  class Status(override val status: Int) extends SimpleResult(header = ResponseHeader(status), body = "status=" + status.toString) {

    def withHeaders(tuple: (String, String)) = this

    /**
     * Set the result's content.
     *
     * @param content content to send
     */
    def apply(content: String): Result = SimpleResult(ResponseHeader(status), content)


  }

  //  def Async(promise: Future[Result]) = AsyncResult(promise)

  /** Generates a ‘200 OK’ result. */
  val Ok = new Status(OK)

  /** Generates a ‘201 CREATED’ result. */
  val Created = new Status(CREATED)

  /** Generates a ‘202 ACCEPTED’ result. */
  val Accepted = new Status(ACCEPTED)

  /** Generates a ‘203 NON_AUTHORITATIVE_INFORMATION’ result. */
  val NonAuthoritativeInformation = new Status(NON_AUTHORITATIVE_INFORMATION)

  //  /** Generates a ‘204 NO_CONTENT’ result. */
  //  val NoContent = SimpleResult(header = ResponseHeader(NO_CONTENT), body = Enumerator(Results.EmptyContent()))
  //
  //  /** Generates a ‘205 RESET_CONTENT’ result. */
  //  val ResetContent = SimpleResult(header = ResponseHeader(RESET_CONTENT), body = Enumerator(Results.EmptyContent()))

  /** Generates a ‘206 PARTIAL_CONTENT’ result. */
  val PartialContent = new Status(PARTIAL_CONTENT)

  /** Generates a ‘207 MULTI_STATUS’ result. */
  val MultiStatus = new Status(MULTI_STATUS)

  /**
   * Generates a ‘301 MOVED_PERMANENTLY’ simple result.
   *
   * @param url the URL to redirect to
   */
  def MovedPermanently(url: String): SimpleResult = Redirect(url, MOVED_PERMANENTLY)

  /**
   * Generates a ‘302 FOUND’ simple result.
   *
   * @param url the URL to redirect to
   */
  def Found(url: String): SimpleResult = Redirect(url, FOUND)

  /**
   * Generates a ‘303 SEE_OTHER’ simple result.
   *
   * @param url the URL to redirect to
   */
  def SeeOther(url: String): SimpleResult = Redirect(url, SEE_OTHER)

  /** Generates a ‘304 NOT_MODIFIED’ result. */
  val NotModified = SimpleResult(header = ResponseHeader(NOT_MODIFIED), body = "")

  /**
   * Generates a ‘307 TEMPORARY_REDIRECT’ simple result.
   *
   * @param url the URL to redirect to
   */
  def TemporaryRedirect(url: String): SimpleResult = Redirect(url, TEMPORARY_REDIRECT)

  /** Generates a ‘400 BAD_REQUEST’ result. */
  val BadRequest = new Status(BAD_REQUEST)

  /** Generates a ‘401 UNAUTHORIZED’ result. */
  val Unauthorized = new Status(UNAUTHORIZED)

  /** Generates a ‘403 FORBIDDEN’ result. */
  val Forbidden = new Status(FORBIDDEN)

  /** Generates a ‘404 NOT_FOUND’ result. */
  val NotFound = new Status(NOT_FOUND)

  /** Generates a ‘405 METHOD_NOT_ALLOWED’ result. */
  val MethodNotAllowed = new Status(METHOD_NOT_ALLOWED)

  /** Generates a ‘406 NOT_ACCEPTABLE’ result. */
  val NotAcceptable = new Status(NOT_ACCEPTABLE)

  /** Generates a ‘408 REQUEST_TIMEOUT’ result. */
  val RequestTimeout = new Status(REQUEST_TIMEOUT)

  /** Generates a ‘409 CONFLICT’ result. */
  val Conflict = new Status(CONFLICT)

  /** Generates a ‘410 GONE’ result. */
  val Gone = new Status(GONE)

  /** Generates a ‘412 PRECONDITION_FAILED’ result. */
  val PreconditionFailed = new Status(PRECONDITION_FAILED)

  /** Generates a ‘413 REQUEST_ENTITY_TOO_LARGE’ result. */
  val EntityTooLarge = new Status(REQUEST_ENTITY_TOO_LARGE)

  /** Generates a ‘414 REQUEST_URI_TOO_LONG’ result. */
  val UriTooLong = new Status(REQUEST_URI_TOO_LONG)

  /** Generates a ‘415 UNSUPPORTED_MEDIA_TYPE’ result. */
  val UnsupportedMediaType = new Status(UNSUPPORTED_MEDIA_TYPE)

  /** Generates a ‘417 EXPECTATION_FAILED’ result. */
  val ExpectationFailed = new Status(EXPECTATION_FAILED)

  /** Generates a ‘422 UNPROCESSABLE_ENTITY’ result. */
  val UnprocessableEntity = new Status(UNPROCESSABLE_ENTITY)

  /** Generates a ‘423 LOCKED’ result. */
  val Locked = new Status(LOCKED)

  /** Generates a ‘424 FAILED_DEPENDENCY’ result. */
  val FailedDependency = new Status(FAILED_DEPENDENCY)

  /** Generates a ‘429 TOO_MANY_REQUEST’ result. */
  val TooManyRequest = new Status(TOO_MANY_REQUEST)

  /** Generates a ‘500 INTERNAL_SERVER_ERROR’ result. */
  val InternalServerError = new Status(INTERNAL_SERVER_ERROR)

  /** Generates a ‘501 NOT_IMPLEMENTED’ result. */
  val NotImplemented = new Status(NOT_IMPLEMENTED)

  /** Generates a ‘502 BAD_GATEWAY’ result. */
  val BadGateway = new Status(BAD_GATEWAY)

  /** Generates a ‘503 SERVICE_UNAVAILABLE’ result. */
  val ServiceUnavailable = new Status(SERVICE_UNAVAILABLE)

  /** Generates a ‘504 GATEWAY_TIMEOUT’ result. */
  val GatewayTimeout = new Status(GATEWAY_TIMEOUT)

  /** Generates a ‘505 HTTP_VERSION_NOT_SUPPORTED’ result. */
  val HttpVersionNotSupported = new Status(HTTP_VERSION_NOT_SUPPORTED)

  /** Generates a ‘507 INSUFFICIENT_STORAGE’ result. */
  val InsufficientStorage = new Status(INSUFFICIENT_STORAGE)

  /**
   * Generates a simple result.
   *
   * @param code the status code
   */
  def Status(code: Int) = new Status(code)

  /**
   * Generates a redirect simple result.
   *
   * @param url the URL to redirect to
   * @param status HTTP status
   */
  def Redirect(url: String, status: Int): SimpleResult = Redirect(url, Map.empty, status)

  /**
   * Generates a redirect simple result.
   *
   * @param url the URL to redirect to
   * @param queryString queryString parameters to add to the queryString
   * @param status HTTP status
   */
  def Redirect(url: String, queryString: Map[String, Seq[String]] = Map.empty, status: Int = SEE_OTHER) = {
    import java.net.URLEncoder
    val fullUrl = url + Option(queryString).filterNot(_.isEmpty).map {
      params =>
        (if (url.contains("?")) "&" else "?") + params.toSeq.flatMap {
          pair =>
            pair._2.map(value => (pair._1 + "=" + URLEncoder.encode(value, "utf-8")))
        }.mkString("&")
    }.getOrElse("")
    Status(status).withHeaders(LOCATION -> fullUrl)
  }

  //    /**
  //     * Generates a redirect simple result.
  //     *
  //     * @param call Call defining the URL to redirect to, which typically comes from the reverse router
  //     */
  //    def Redirect(call: Call): SimpleResult[Results.EmptyContent] = Redirect(call.url)

}

/**
 * A simple HTTP response header, used for standard responses.
 *
 * @param status the response status, e.g. ‘200 OK’
 * @param headers the HTTP headers
 */
case class ResponseHeader(status: Int, headers: Map[String, String] = Map.empty) {

  override def toString = {
    status + ", " + headers
  }

}

/**
 * A simple result, which defines the response header and a body ready to send to the client.
 *
 * @param header the response header, which contains status code and HTTP headers
 * @param body the response body
 */
case class SimpleResult(header: ResponseHeader, body: String) extends PlainResult {

  val status = header.status

  /**
   * Adds headers to this result.
   *
   * For example:
   * {{{
   * Ok("Hello world").withHeaders(ETAG -> "0")
   * }}}
   *
   * @param headers the headers to add to this result.
   * @return the new result
   */
  def withHeaders(headers: (String, String)*) = {
    copy(header = header.copy(headers = header.headers ++ headers))
  }

  override def toString = {
    "SimpleResult(" + header + ")"
  }

}

sealed trait WithHeaders[+A <: Result] {
  /**
   * Adds HTTP headers to this result.
   *
   * For example:
   * {{{
   * Ok("Hello world").withHeaders(ETAG -> "0")
   * }}}
   *
   * @param headers the headers to add to this result.
   * @return the new result
   */
  def withHeaders(headers: (String, String)*): A

  /**
   * Adds cookies to this result.
   *
   * For example:
   * {{{
   * Ok("Hello world").withCookies(Cookie("theme", "blue"))
   * }}}
   *
   * @param cookies the cookies to add to this result
   * @return the new result
   */
  def withCookies(cookies: Cookie*): A


  /**
   * Discards cookies along this result.
   *
   * For example:
   * {{{
   * Ok("Hello world").discardingCookies("theme")
   * }}}
   *
   * @param names the names of the cookies to discard along to this result
   * @return the new result
   */
  @deprecated("This method can only discard cookies on the / path with no domain and without secure set.  Use discardingCookies(DiscardingCookie*) instead.", "2.1")
  def discardingCookies(name: String, names: String*): A = discardingCookies((name :: names.toList).map(n => DiscardingCookie(n)): _*)

  /**
   * Discards cookies along this result.
   *
   * For example:
   * {{{
   * Ok("Hello world").discardingCookies(DiscardingCookie("theme"))
   * }}}
   *
   * @param cookies the cookies to discard along to this result
   * @return the new result
   */
  def discardingCookies(cookies: DiscardingCookie*): A


  /**
   * Changes the result content type.
   *
   * For example:
   * {{{
   * Ok("<text>Hello world</text>").as("text/xml")
   * }}}
   *
   * @param contentType the new content type.
   * @return the new result
   */
  def as(contentType: String): A
}

sealed trait Result extends NotNull with WithHeaders[Result] {
    def body: String
    val status: Int
}

/**
 * A plain HTTP result.
 */
trait PlainResult extends Result with WithHeaders[PlainResult] {

  /**
   * The response header
   */
  val header: ResponseHeader

  /**
   * Adds cookies to this result.
   *
   * For example:
   * {{{
   * Ok("Hello world").withCookies(Cookie("theme", "blue"))
   * }}}
   *
   * @param cookies the cookies to add to this result
   * @return the new result
   */
  def withCookies(cookies: Cookie*): PlainResult = {
    withHeaders(SET_COOKIE -> Cookies.merge(header.headers.get(SET_COOKIE).getOrElse(""), cookies))
  }

  /**
   * Discards cookies along this result.
   *
   * For example:
   * {{{
   * Ok("Hello world").discardingCookies("theme")
   * }}}
   *
   * @param cookies the cookies to discard along to this result
   * @return the new result
   */
  def discardingCookies(cookies: DiscardingCookie*): PlainResult = {
    withHeaders(SET_COOKIE -> Cookies.merge(header.headers.get(SET_COOKIE).getOrElse(""), cookies.map(_.toCookie)))
  }


  /**
   * Changes the result content type.
   *
   * For example:
   * {{{
   * Ok("<text>Hello world</text>").as("text/xml")
   * }}}
   *
   * @param contentType the new content type.
   * @return the new result
   */
  def as(contentType: String): PlainResult = withHeaders(CONTENT_TYPE -> contentType)
}

object Cookies {

  /**
   * Extract cookies from the Set-Cookie header.
   */
  def apply(header: Option[String]) = new Cookies {

    lazy val cookies: Map[String, Cookie] = header.map(Cookies.decode(_)).getOrElse(Seq.empty).groupBy(_.name).mapValues(_.head)

    def get(name: String) = cookies.get(name)

    override def toString = cookies.toString

  }

  /**
   * Encodes cookies as a proper HTTP header.
   *
   * @param cookies the Cookies to encode
   * @return a valid Set-Cookie header value
   */
  def encode(cookies: Seq[Cookie]): String = {
    ""
  }

  /**
   * Decodes a Set-Cookie header value as a proper cookie set.
   *
   * @param cookieHeader the Set-Cookie header value
   * @return decoded cookies
   */
  def decode(cookieHeader: String): Seq[Cookie] = {
    Seq()
  }

  /**
   * Merges an existing Set-Cookie header with new cookie values
   *
   * @param cookieHeader the existing Set-Cookie header value
   * @param cookies the new cookies to encode
   * @return a valid Set-Cookie header value
   */
  def merge(cookieHeader: String, cookies: Seq[Cookie]): String = {
    ""
  }
}
