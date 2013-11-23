package scala.concurrent {

import java.util.concurrent.Executor

/**
 * Contains factory methods for creating execution contexts.
 */
object ExecutionContextAE {
  /**
   * This is the explicit global ExecutionContext,
   * call this when you want to provide the global ExecutionContext explicitly
   */
  def global: ExecutionContextExecutor = ImplicitsAE.global

  object ImplicitsAE {
    /**
     * This is the implicit global ExecutionContext,
     * import this when you want to provide the global ExecutionContext implicitly
     */
    implicit lazy val global: ExecutionContextExecutor = impl.ExecutionContextAppEngineImpl.fromExecutor(null: Executor)
  }

}

}