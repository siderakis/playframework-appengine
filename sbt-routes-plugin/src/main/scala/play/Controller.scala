package play

import play.api.mvc._

import scala.concurrent.Future
import play.api.http.{HeaderNames, Status}


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

  import play.api._
  import play.api.http.Status._
  import play.api.http.HeaderNames._


  /**
   * Generates default `SimpleResult` from a content type, headers and content.
   *
   * @param status the HTTP response status, e.g ‘200 OK’
   */
  class Status(val status: Int) extends Result {
    def body: String = "status=" + status.toString


    /**
     * Set the result's content.
     *
     * @param content content to send
     */
    def apply(content: String): Result = {
      new Result {
        def body: String = content

        val status: Int = Status.this.status
      }

    }

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

  //  /**
  //   * Generates a ‘301 MOVED_PERMANENTLY’ simple result.
  //   *
  //   * @param url the URL to redirect to
  //   */
  //  def MovedPermanently(url: String): SimpleResult[Results.EmptyContent] = Redirect(url, MOVED_PERMANENTLY)
  //
  //  /**
  //   * Generates a ‘302 FOUND’ simple result.
  //   *
  //   * @param url the URL to redirect to
  //   */
  //  def Found(url: String): SimpleResult[Results.EmptyContent] = Redirect(url, FOUND)
  //
  //  /**
  //   * Generates a ‘303 SEE_OTHER’ simple result.
  //   *
  //   * @param url the URL to redirect to
  //   */
  //  def SeeOther(url: String): SimpleResult[Results.EmptyContent] = Redirect(url, SEE_OTHER)
  //
  //  /** Generates a ‘304 NOT_MODIFIED’ result. */
  //  val NotModified = SimpleResult(header = ResponseHeader(NOT_MODIFIED), body = Enumerator(Results.EmptyContent()))
  //
  //  /**
  //   * Generates a ‘307 TEMPORARY_REDIRECT’ simple result.
  //   *
  //   * @param url the URL to redirect to
  //   */
  //  def TemporaryRedirect(url: String): SimpleResult[Results.EmptyContent] = Redirect(url, TEMPORARY_REDIRECT)

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

  //  /**
  //   * Generates a redirect simple result.
  //   *
  //   * @param url the URL to redirect to
  //   * @param status HTTP status
  //   */
  //  def Redirect(url: String, status: Int): SimpleResult[Results.EmptyContent] = Redirect(url, Map.empty, status)
  //
  //  /**
  //   * Generates a redirect simple result.
  //   *
  //   * @param url the URL to redirect to
  //   * @param queryString queryString parameters to add to the queryString
  //   * @param status HTTP status
  //   */
  //  def Redirect(url: String, queryString: Map[String, Seq[String]] = Map.empty, status: Int = SEE_OTHER) = {
  //    import java.net.URLEncoder
  //    val fullUrl = url + Option(queryString).filterNot(_.isEmpty).map { params =>
  //      (if (url.contains("?")) "&" else "?") + params.toSeq.flatMap { pair =>
  //        pair._2.map(value => (pair._1 + "=" + URLEncoder.encode(value, "utf-8")))
  //      }.mkString("&")
  //    }.getOrElse("")
  //    Status(status).withHeaders(LOCATION -> fullUrl)
  //  }
  //
  //  /**
  //   * Generates a redirect simple result.
  //   *
  //   * @param call Call defining the URL to redirect to, which typically comes from the reverse router
  //   */
  //  def Redirect(call: Call): SimpleResult[Results.EmptyContent] = Redirect(call.url)

}


