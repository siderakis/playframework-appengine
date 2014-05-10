package play.api.libs {

/**
 * The Iteratee monad provides strict, safe, and functional I/O.
 */
package object iteratee {

  type K[E, A] = Input[E] => Iteratee[E, A]

}

}

package play.api.libs.iteratee {

import com.google.appengine.api.ThreadManager

private[iteratee] object internal {
  import scala.concurrent.ExecutionContext
  import java.util.concurrent.Executors

  implicit lazy val defaultExecutionContext: scala.concurrent.ExecutionContext = {
    val numberOfThreads = 10
    val threadFactory = ThreadManager.backgroundThreadFactory()


    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(numberOfThreads, threadFactory))
  }
}
}




/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.concurrent.impl{

//import scala.concurrent.impl.Promise
import scala.concurrent.{ExecutionContext, CanAwait, OnCompleteRunnable, TimeoutException, ExecutionException}
import scala.concurrent.duration.{Duration, Deadline, FiniteDuration}
import scala.annotation.tailrec
import scala.util.control.NonFatal
import scala.util.{Try, Success, Failure}

private[concurrent] trait Promise[T] extends scala.concurrent.Promise[T] with scala.concurrent.Future[T] {
  def future: this.type = this
}

/* Precondition: `executor` is prepared, i.e., `executor` has been returned from invocation of `prepare` on some other `ExecutionContext`.
 */
private class CallbackRunnable[T](val executor: ExecutionContext, val onComplete: Try[T] => Any) extends Runnable with OnCompleteRunnable {
  // must be filled in before running it
  var value: Try[T] = null

  override def run() = {
    require(value ne null) // must set value to non-null before running!
    try onComplete(value) catch {
      case NonFatal(e) => executor reportFailure e
    }
  }

  def executeWithValue(v: Try[T]): Unit = {
    require(value eq null) // can't complete it twice
    value = v
    // Note that we cannot prepare the ExecutionContext at this point, since we might
    // already be running on a different thread!
    try executor.execute(this) catch {
      case NonFatal(t) => executor reportFailure t
    }
  }
}

private[concurrent] object Promise {

  /** An already completed Future is given its result at creation.
    *
    * Useful in Future-composition when a value to contribute is already available.
    */
  final class KeptPromise[T](suppliedValue: Try[T]) extends Promise[T] {

    val value = Some(resolveTry(suppliedValue))

    override def isCompleted: Boolean = true

    def tryComplete(value: Try[T]): Boolean = false

    def onComplete[U](func: Try[T] => U)(implicit executor: ExecutionContext): Unit = {
      val completedAs = value.get
      val preparedEC = executor.prepare
      (new CallbackRunnable(preparedEC, func)).executeWithValue(completedAs)
    }

    def ready(atMost: Duration)(implicit permit: CanAwait): this.type = this

    def result(atMost: Duration)(implicit permit: CanAwait): T = value.get.get
  }

  private def resolveTry[T](source: Try[T]): Try[T] = source match {
    case Failure(t) => resolver(t)
    case _ => source
  }

  private def resolver[T](throwable: Throwable): Try[T] = throwable match {
    case t: scala.runtime.NonLocalReturnControl[_] => Success(t.value.asInstanceOf[T])
    case t: scala.util.control.ControlThrowable => Failure(new ExecutionException("Boxed ControlThrowable", t))
    case t: InterruptedException => Failure(new ExecutionException("Boxed InterruptedException", t))
    case e: Error => Failure(new ExecutionException("Boxed Error", e))
    case t => Failure(t)
  }

  /**
   * App Engine promise implementation.
   */
  class DefaultPromise[T] extends Object with Promise[T] {
    self =>

    /**
     * Atomically update variable to <tt>newState</tt> if it is currently
     * holding <tt>oldState</tt>.
     * @return <tt>true</tt> if successful
     */
    def updateState(oldState: AnyRef, newState: AnyRef) = {
      // println(s"updateState: $oldState and $newState")
      obj.synchronized(
        if (obj == oldState) {
          obj = newState
          true
        } else {
          false
        }
      )
    }

    @volatile var obj: AnyRef = Nil

    def getState() = {
      obj.synchronized(obj)
    }

    // Start at "No callbacks"
    updateState(null, Nil)


    protected final def tryAwait(atMost: Duration): Boolean = {
      @tailrec
      def awaitUnsafe(deadline: Deadline, nextWait: FiniteDuration): Boolean = {
        if (!isCompleted && nextWait > Duration.Zero) {
          val ms = nextWait.toMillis
          val ns = (nextWait.toNanos % 1000000l).toInt // as per object.wait spec

          synchronized {
            if (!isCompleted) wait(ms, ns)
          }

          awaitUnsafe(deadline, deadline.timeLeft)
        } else
          isCompleted
      }
      @tailrec
      def awaitUnbounded(): Boolean = {
        if (isCompleted) true
        else {
          synchronized {
            if (!isCompleted) wait()
          }
          awaitUnbounded()
        }
      }

      import Duration.Undefined
      atMost match {
        case u if u eq Undefined => throw new IllegalArgumentException("cannot wait for Undefined period")
        case Duration.Inf => awaitUnbounded
        case Duration.MinusInf => isCompleted
        case f: FiniteDuration => if (f > Duration.Zero) awaitUnsafe(f.fromNow, f) else isCompleted
      }
    }

    @throws(classOf[TimeoutException])
    @throws(classOf[InterruptedException])
    def ready(atMost: Duration)(implicit permit: CanAwait): this.type =
      if (isCompleted || tryAwait(atMost)) this
      else throw new TimeoutException("Futures timed out after [" + atMost + "]")

    @throws(classOf[Exception])
    def result(atMost: Duration)(implicit permit: CanAwait): T =
      ready(atMost).value.get match {
        case Failure(e) => throw e
        case Success(r) => r
      }

    def value: Option[Try[T]] = getState match {
      case c: Try[_] => Some(c.asInstanceOf[Try[T]])
      case _ => None
    }

    override def isCompleted: Boolean = getState match {
      // Cheaper than boxing result into Option due to "def value"
      case _: Try[_] => true
      case _ => false
    }

    def tryComplete(value: Try[T]): Boolean = {
      val resolved = resolveTry(value)
      (try {
        @tailrec
        def tryComplete(v: Try[T]): List[CallbackRunnable[T]] = {
          getState match {
            case raw: List[_] =>
              val cur = raw.asInstanceOf[List[CallbackRunnable[T]]]
              if (updateState(cur, v)) cur else tryComplete(v)
            case _ => null
          }
        }
        tryComplete(resolved)
      } finally {
        synchronized {
          notifyAll()
        } //Notify any evil blockers
      }) match {
        case null => false
        case rs if rs.isEmpty => true
        case rs => rs.foreach(r => r.executeWithValue(resolved)); true
      }
    }

    def onComplete[U](func: Try[T] => U)(implicit executor: ExecutionContext): Unit = {
      val preparedEC = executor.prepare
      val runnable = new CallbackRunnable[T](preparedEC, func)

      @tailrec //Tries to add the callback, if already completed, it dispatches the callback to be executed
      def dispatchOrAddCallback(): Unit =
        getState match {
          case r: Try[_] => runnable.executeWithValue(r.asInstanceOf[Try[T]])
          case listeners: List[_] => if (updateState(listeners, runnable :: listeners)) () else dispatchOrAddCallback()
        }
      dispatchOrAddCallback()
    }





  /** Link this promise to the root of another promise using `link()`. Should only be
    *  be called by Future.flatMap.
    */
  protected[concurrent] final def linkRootOf(target: DefaultPromise[T]): Unit = link(target.compressedRoot())



    /** Get the promise at the root of the chain of linked promises. Used by `compressedRoot()`.
      *  The `compressedRoot()` method should be called instead of this method, as it is important
      *  to compress the link chain whenever possible.
      */
    @tailrec
    private def root: DefaultPromise[T] = {
      getState match {
        case linked: DefaultPromise[_] => linked.asInstanceOf[DefaultPromise[T]].root
        case _ => this
      }
    }

  /** Get the root promise for this promise, compressing the link chain to that
    *  promise if necessary.
    *
    *  For promises that are not linked, the result of calling
    *  `compressedRoot()` will the promise itself. However for linked promises,
    *  this method will traverse each link until it locates the root promise at
    *  the base of the link chain.
    *
    *  As a side effect of calling this method, the link from this promise back
    *  to the root promise will be updated ("compressed") to point directly to
    *  the root promise. This allows intermediate promises in the link chain to
    *  be garbage collected. Also, subsequent calls to this method should be
    *  faster as the link chain will be shorter.
    */
  @tailrec
  private def compressedRoot(): DefaultPromise[T] = {
    getState match {
      case linked: DefaultPromise[_] =>
        val target = linked.asInstanceOf[DefaultPromise[T]].root
        if (linked eq target) target else if (updateState(linked, target)) target else compressedRoot()
      case _ => this
    }
  }

    /** Tries to add the callback, if already completed, it dispatches the callback to be executed.
      *  Used by `onComplete()` to add callbacks to a promise and by `link()` to transfer callbacks
      *  to the root promise when linking two promises togehter.
      */
    @tailrec
    private def dispatchOrAddCallback(runnable: CallbackRunnable[T]): Unit = {
      getState match {
        case r: Try[_]          => runnable.executeWithValue(r.asInstanceOf[Try[T]])
        case _: DefaultPromise[_] => compressedRoot().dispatchOrAddCallback(runnable)
        case listeners: List[_] => if (updateState(listeners, runnable :: listeners)) () else dispatchOrAddCallback(runnable)
      }
    }

  /** Link this promise to another promise so that both promises share the same
    *  externally-visible state. Depending on the current state of this promise, this
    *  may involve different things. For example, any onComplete listeners will need
    *  to be transferred.
    *
    *  If this promise is already completed, then the same effect as linking -
    *  sharing the same completed value - is achieved by simply sending this
    *  promise's result to the target promise.
    */
  @tailrec
  private def link(target: DefaultPromise[T]): Unit = if (this ne target) {
    getState match {
      case r: Try[_] =>
        if (!target.tryComplete(r.asInstanceOf[Try[T]])) {
          // Currently linking is done from Future.flatMap, which should ensure only
          // one promise can be completed. Therefore this situation is unexpected.
          throw new IllegalStateException("Cannot link completed promises together")
        }
      case _: DefaultPromise[_] =>
        compressedRoot().link(target)
      case listeners: List[_] => if (updateState(listeners, target)) {
        if (!listeners.isEmpty) listeners.asInstanceOf[List[CallbackRunnable[T]]].foreach(target.dispatchOrAddCallback(_))
      } else link(target)
    }
  }
}


}


}

package scala.concurrent{

  import scala.language.higherKinds

  import java.util.concurrent.{TimeUnit}
  import scala.concurrent.{Future, ExecutionContext, Promise => SPromise}
  import scala.util.{Failure, Success, Try}
  import scala.concurrent.duration.FiniteDuration


/** Promise is an object which can be completed with a value or failed
  *  with an exception.
  *
  *  @define promiseCompletion
  *  If the promise has already been fulfilled, failed or has timed out,
  *  calling this method will throw an IllegalStateException.
  *
  *  @define allowedThrowables
  *  If the throwable used to fail this promise is an error, a control exception
  *  or an interrupted exception, it will be wrapped as a cause within an
  *  `ExecutionException` which will fail the promise.
  *
  *  @define nonDeterministic
  *  Note: Using this method may result in non-deterministic concurrent programs.
  */
trait Promise[T] {

  // used for internal callbacks defined in
  // the lexical scope of this trait;
  // _never_ for application callbacks.
  private implicit def internalExecutor: ExecutionContext = Future.InternalCallbackExecutor

  /** Future containing the value of this promise.
    */
  def future: Future[T]

  /** Returns whether the promise has already been completed with
    *  a value or an exception.
    *
    *  $nonDeterministic
    *
    *  @return    `true` if the promise is already completed, `false` otherwise
    */
  def isCompleted: Boolean

  /** Completes the promise with either an exception or a value.
    *
    *  @param result     Either the value or the exception to complete the promise with.
    *
    *  $promiseCompletion
    */
  def complete(result: Try[T]): this.type =
    if (tryComplete(result)) this else throw new IllegalStateException("Promise already completed.")

  /** Tries to complete the promise with either a value or the exception.
    *
    *  $nonDeterministic
    *
    *  @return    If the promise has already been completed returns `false`, or `true` otherwise.
    */
  def tryComplete(result: Try[T]): Boolean

  /** Completes this promise with the specified future, once that future is completed.
    *
    *  @return   This promise
    */
  final def completeWith(other: Future[T]): this.type = {
    other onComplete { this complete _ }
    this
  }

  /** Attempts to complete this promise with the specified future, once that future is completed.
    *
    *  @return   This promise
    */
  final def tryCompleteWith(other: Future[T]): this.type = {
    other onComplete { this tryComplete _ }
    this
  }

  /** Completes the promise with a value.
    *
    *  @param v    The value to complete the promise with.
    *
    *  $promiseCompletion
    */
  def success(v: T): this.type = complete(Success(v))

  /** Tries to complete the promise with a value.
    *
    *  $nonDeterministic
    *
    *  @return    If the promise has already been completed returns `false`, or `true` otherwise.
    */
  def trySuccess(value: T): Boolean = tryComplete(Success(value))

  /** Completes the promise with an exception.
    *
    *  @param t        The throwable to complete the promise with.
    *
    *  $allowedThrowables
    *
    *  $promiseCompletion
    */
  def failure(t: Throwable): this.type = complete(Failure(t))

  /** Tries to complete the promise with an exception.
    *
    *  $nonDeterministic
    *
    *  @return    If the promise has already been completed returns `false`, or `true` otherwise.
    */
  def tryFailure(t: Throwable): Boolean = tryComplete(Failure(t))
}

/**
   * useful helper methods to create and compose Promises
   */
  object Promise {

    /** Creates a promise object which can be completed with a value.
      *
      *  @tparam T       the type of the value in the promise
      *  @return         the newly created `Promise` object
      */
    def apply[T](): Promise[T] = new impl.Promise.DefaultPromise[T]()

    /** Creates an already completed Promise with the specified exception.
      *
      *  @tparam T       the type of the value in the promise
      *  @return         the newly created `Promise` object
      */
    def failed[T](exception: Throwable): Promise[T] = fromTry(Failure(exception))

    /** Creates an already completed Promise with the specified result.
      *
      *  @tparam T       the type of the value in the promise
      *  @return         the newly created `Promise` object
      */
    def successful[T](result: T): Promise[T] = fromTry(Success(result))

    /** Creates an already completed Promise with the specified result or exception.
      *
      *  @tparam T       the type of the value in the promise
      *  @return         the newly created `Promise` object
      */
    def fromTry[T](result: Try[T]): Promise[T] = new impl.Promise.KeptPromise[T](result)

    /**
     * Constructs a Future which will contain value "message" after the given duration elapses.
     * This is useful only when used in conjunction with other Promises
     * @param message message to be displayed
     * @param duration duration for the scheduled promise
     * @return a scheduled promise
     */
    def timeout[A](message: => A, duration: scala.concurrent.duration.Duration)(implicit ec: ExecutionContext): Future[A] = {
      timeout(message, duration.toMillis)
    }

    /**
     * Constructs a Future which will contain value "message" after the given duration elapses.
     * This is useful only when used in conjunction with other Promises
     * @param message message to be displayed
     * @param duration duration for the scheduled promise
     * @return a scheduled promise
     */
    def timeout[A](message: => A, duration: Long, unit: TimeUnit = TimeUnit.MILLISECONDS)(implicit ec: ExecutionContext): Future[A] = {
      val p = SPromise[A]()
      //import play.api.Play.current
      //Akka.system.scheduler.scheduleOnce(FiniteDuration(duration, unit)) {
      Thread.sleep(FiniteDuration(duration, unit).toMillis)
      p.complete(Try(message))
      //}
      p.future
    }

  }
}