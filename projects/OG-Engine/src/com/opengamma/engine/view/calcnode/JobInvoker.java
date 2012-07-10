/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;

/**
 * Something that can invoke a job when required by the JobDispatcher.
 * <p>
 * Capabilities are calculated at an invoker level so an invoker should only dispatch to a
 * homogeneous set of calculation nodes. The calculation nodes a given job invoker dispatches
 * to are assumed to share the same local cache.
 */
public interface JobInvoker {

  /**
   * Returns the exported capabilities of the node(s) this invoker is responsible for.
   * <p>
   * These will be used to determine which jobs will get farmed to this invoker.
   * It will not be modified by the job dispatcher.
   * An iterator on the collection must return the capabilities in their natural order.
   * It should have very efficient hashCode and equals operations.
   * 
   * @return the capabilities, not null
   */
  Collection<Capability> getCapabilities();

  /**
   * Invokes a job on a calculation node.
   * 
   * @param job  the job to run, not null
   * @param receiver  the result receiver; must be signaled with either success or failure unless
   *  false is returned to indicate the invoker did not accept the job, not null
   * @return true if the invoker has caused the job to execute or false}if
   * capacity problems mean it cannot be executed. After returning false to the
   * dispatcher the invoker will be unregistered.
   */
  boolean invoke(CalculationJob job, JobInvocationReceiver receiver);

  /**
   * Called after invocation failure for the invoker to notify the dispatch object if/when
   * it becomes available again.
   * 
   * @param callback  the object the invoker should register itself with when it is ready to
   * receive {@link #invoke} calls again. This must not be called inline from this method,
   * if the invoker is ready it must return true.
   * @return true if the invoker is ready now, false if the callback will be invoked in the future
   */
  boolean notifyWhenAvailable(JobInvokerRegister callback);

  /**
   * Gets the identifier of the invoker.
   * 
   * @return the identifier, not null
   */
  String getInvokerId();

  /**
   * Attempts to cancel the set of jobs previously started by a call to
   * {@link #invoke(CalculationJob, JobInvocationReceiver)}.
   * <p>
   * After cancellation the job should not generate a callback to the invocation receiver,
   * but may do so if cancellation is not possible.
   * 
   * @param jobs  the jobs to cancel, not null
   */
  void cancel(Collection<CalculationJobSpecification> jobs);

  void cancel(CalculationJobSpecification job);

  /**
   * Queries the status of jobs on the invoker.
   * <p>
   * This can be used as a "hint" or nudge to help failed nodes abort sooner and allow
   * calculation to resume elsewhere.
   * 
   * @param jobs  the outstanding jobs thought to be still running with this invoker
   * @return true if the invoker is confident the jobs will complete, false if
   * the jobs must be considered "timed-out" and re-dispatched. This method must not block or
   * take long to complete. If determining status is costly, the node should return true
   * and use {@link JobInvocationReceiver#jobFailed(JobInvoker, String, Exception)}
   * asynchronously.
   */
  boolean isAlive(Collection<CalculationJobSpecification> jobs);

  boolean isAlive(CalculationJobSpecification job);

}
