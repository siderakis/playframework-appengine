package scala.concurrent.impl


import java.util.concurrent._
import scala.concurrent.ExecutionContextExecutor
import com.google.appengine.api.ThreadManager
import scala.collection.mutable


class ExecutionContextAppEngineImpl2(es: Executor, reporter: Throwable => Unit) extends ExecutionContextExecutor {

  val threads = mutable.Set[Thread]()


  val executor: ExecutorService = {

    val e = Executors.newFixedThreadPool(10, new ThreadFactory {
      val impl = ThreadManager.currentRequestThreadFactory()

      def newThread(r: Runnable): Thread = {
        val t = impl.newThread(r)
        println(s"Creating new Thread ${t.getName} by: " + Thread.currentThread().getName)
        threads += t
        t
      }

    })

    e

  }

  def block() = {

    // can't submit new work after this exe
    // executor.shutdown()
    //executor.awaitTermination(1, TimeUnit.MINUTES)

    // -- or --

    //    threads.foreach {
    //      t =>
    //
    //        println(s"BLOCKING ON $t")
    //        t.join() // doesn't return
    //        println(s"DONE BLOCKING ON $t")
    //    }
  }

  def execute(runnable: Runnable): Unit = executor execute runnable

  def reportFailure(t: Throwable) = reporter(t)
}
