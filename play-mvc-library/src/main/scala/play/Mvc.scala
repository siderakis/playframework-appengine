package play.api.mvc

import play.Result
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


/**
 * Provides helpers for creating `Action` values.
 */
trait ActionBuilder {

  /**
   * Constructs an `Action`.
   *
   * For example:
   * {{{
   * val echo = Action(parse.anyContent) { request =>
   *   Ok("Got request [" + request + "]")
   * }
   * }}}
   *
   * @param block the action code
   * @return an action
   */
  def apply(block: RequestHeader => Result): Action = new Action {


    def apply(ctx: RequestHeader) = try {
      block(ctx)

    } catch {
      // NotImplementedError is not caught by NonFatal, wrap it
      case e: NotImplementedError => throw new RuntimeException(e)
      // LinkageError is similarly harmless in Play Framework, since automatic reloading could easily trigger it
      case e: LinkageError => throw new RuntimeException(e)
    }
  }

  /**
   * Constructs an `Action` with default content, and no request parameter.
   *
   * For example:
   * {{{
   * val hello = Action {
   *   Ok("Hello!")
   * }
   * }}}
   *
   * @param block the action code
   * @return an action
   */
  def apply(block: => Result): Action = apply(_ => block)


  def async(block: RequestHeader => Future[Result]): Action = new Action {

    def apply(ctx: RequestHeader) = try {
      Await.result(block(ctx), 1 hour)
    } catch {
      // NotImplementedError is not caught by NonFatal, wrap it
      case e: NotImplementedError => throw new RuntimeException(e)
      // LinkageError is similarly harmless in Play Framework, since automatic reloading could easily trigger it
      case e: LinkageError => throw new RuntimeException(e)
    }
  }

  def async(block: => Future[Result]): Action = async(_ => block)


}

/**
 * Helper object to create `Action` values.
 */
object Action extends ActionBuilder


trait Action extends Handler


