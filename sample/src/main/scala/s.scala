/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.concurrent.impl


import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scala.util.{Success, Failure}

class PromiseCompletingRunnables[T](body: => T) extends Runnable {
  val promise = new Promise.DefaultPromise[T]()

  override def run() = {
    promise complete {
      try Success(body) catch {
        case NonFatal(e) => Failure(e)
      }
    }
  }
}

object Futures {

  def apply[T](body: => T)(implicit executor: ExecutionContext): scala.concurrent.Future[T] = {
    println("Future::apply")
    val runnable = new PromiseCompletingRunnables(body)
    executor.prepare.execute(runnable)
    runnable.promise.future
  }
}
