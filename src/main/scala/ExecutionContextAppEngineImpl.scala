package scala.concurrent.impl


import java.util.concurrent._
import java.util.Collection
import scala.concurrent.forkjoin._
import scala.concurrent.{BlockContext, ExecutionContext, CanAwait, ExecutionContextExecutor, ExecutionContextExecutorService}
import scala.util.control.NonFatal
import com.google.appengine.api.ThreadManager
import scala.concurrent.forkjoin.ForkJoinTask
import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.forkjoin.ForkJoinWorkerThread


private[scala] class ExecutionContextAppEngineImpl(es: Executor, reporter: Throwable => Unit) extends ExecutionContextExecutor {
  // Placed here since the creation of the executor needs to read this val
  private[this] val uncaughtExceptionHandler: Thread.UncaughtExceptionHandler = new Thread.UncaughtExceptionHandler {
    def uncaughtException(thread: Thread, cause: Throwable): Unit = reporter(cause)
  }

  val executor: Executor = es match {
    case null => createExecutorService
    case some => some
  }

  // Implement BlockContext on FJP threads
  class DefaultThreadFactory(daemonic: Boolean) extends ThreadFactory with ForkJoinPool.ForkJoinWorkerThreadFactory {
    def wire[T <: Thread](thread: T): T = {
      //thread.setDaemon(daemonic)
      thread.setUncaughtExceptionHandler(uncaughtExceptionHandler)
      thread
    }

    def newThread(runnable: Runnable): Thread = wire(ThreadManager.currentRequestThreadFactory().newThread(runnable))

    def newThread(fjp: ForkJoinPool): ForkJoinWorkerThread = wire(new ForkJoinWorkerThread(fjp) with BlockContext {
      override def blockOn[T](thunk: => T)(implicit permission: CanAwait): T = {
        var result: T = null.asInstanceOf[T]
        ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker {
          @volatile var isdone = false

          override def block(): Boolean = {
            result = try thunk finally {
              isdone = true
            }
            true
          }

          override def isReleasable = isdone
        })
        result
      }
    })
  }

  def createExecutorService: ExecutorService =

    Executors.newCachedThreadPool(ThreadManager.currentRequestThreadFactory())



  def execute(runnable: Runnable): Unit = executor match {
    case fj: ForkJoinPool =>
      val fjt = runnable match {
        case t: ForkJoinTask[_] => t
        case runnable => new ForkJoinTask[Unit] {
          final override def setRawResult(u: Unit): Unit = ()

          final override def getRawResult(): Unit = ()

          final override def exec(): Boolean = try {
            runnable.run(); true
          } catch {
            case anything: Throwable ⇒
              val t = Thread.currentThread
              t.getUncaughtExceptionHandler match {
                case null ⇒
                case some ⇒ some.uncaughtException(t, anything)
              }
              throw anything
          }
        }
      }
      Thread.currentThread match {
        case fjw: ForkJoinWorkerThread if fjw.getPool eq fj => fjt.fork()
        case _ => fj execute fjt
      }
    case generic => generic execute runnable
  }

  def reportFailure(t: Throwable) = reporter(t)
}

private[concurrent] object ExecutionContextAppEngineImpl {
  def fromExecutor(e: Executor, reporter: Throwable => Unit = ExecutionContext.defaultReporter): ExecutionContextAppEngineImpl = new ExecutionContextAppEngineImpl(e, reporter)

  def fromExecutorService(es: ExecutorService, reporter: Throwable => Unit = ExecutionContext.defaultReporter): ExecutionContextAppEngineImpl with ExecutionContextExecutorService =
    new ExecutionContextAppEngineImpl(es, reporter) with ExecutionContextExecutorService {
      final def asExecutorService: ExecutorService = executor.asInstanceOf[ExecutorService]

      override def execute(command: Runnable) = executor.execute(command)

      override def shutdown() {
        asExecutorService.shutdown()
      }

      override def shutdownNow() = asExecutorService.shutdownNow()

      override def isShutdown = asExecutorService.isShutdown

      override def isTerminated = asExecutorService.isTerminated

      override def awaitTermination(l: Long, timeUnit: TimeUnit) = asExecutorService.awaitTermination(l, timeUnit)

      override def submit[T](callable: Callable[T]) = asExecutorService.submit(callable)

      override def submit[T](runnable: Runnable, t: T) = asExecutorService.submit(runnable, t)

      override def submit(runnable: Runnable) = asExecutorService.submit(runnable)

      override def invokeAll[T](callables: Collection[_ <: Callable[T]]) = asExecutorService.invokeAll(callables)

      override def invokeAll[T](callables: Collection[_ <: Callable[T]], l: Long, timeUnit: TimeUnit) = asExecutorService.invokeAll(callables, l, timeUnit)

      override def invokeAny[T](callables: Collection[_ <: Callable[T]]) = asExecutorService.invokeAny(callables)

      override def invokeAny[T](callables: Collection[_ <: Callable[T]], l: Long, timeUnit: TimeUnit) = asExecutorService.invokeAny(callables, l, timeUnit)
    }

}
