/*
 * Zynaptic Reaction - An asynchronous programming framework for Java.
 * 
 * Copyright (c) 2009-2019, Zynaptic Limited.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Please visit www.zynaptic.com or contact reaction@zynaptic.com if you need
 * additional information or have any questions.
 */

package com.zynaptic.reaction;

import java.util.MissingResourceException;

/**
 * Defines the user API for accessing the reactor object. The reactor object
 * provides access to the various Reaction services via this interface. The
 * reactor object is a singleton, so a reference to this interface can only be
 * obtained via the {@link com.zynaptic.reaction.core.ReactorCore#getReactor()
 * getReactor} static method on the
 * {@link com.zynaptic.reaction.core.ReactorCore} class.
 * 
 * @author Chris Holgate
 */
public interface Reactor {

  /**
   * Gets the elapsed time since the reactor was started. This method is used to
   * obtain the current value of the reactor's monotonic clock. This allows other
   * components in the system to use the monotonic clock source when required.
   * 
   * @return Returns the number of milliseconds which have elapsed since the
   *   reactor started up.
   */
  public long getUptime();

  /**
   * Gets the runtime message logger which is associated with the specified logger
   * ID. If a message logger for the specified logger ID already exists this will
   * be returned to the caller. If no message logger for the specified logger ID
   * currently exists a new logger object will be created and returned.
   * 
   * @param loggerId This is the logger ID which is associated with the requested
   *   message logger. This is a String value which will normally hold the
   *   canonical name of the client class which is using the logger.
   * @return Returns a logger object which may be used for logging runtime
   *   messages.
   */
  public Logger getLogger(String loggerId);

  /**
   * Gets the runtime message logger which is associated with the specified logger
   * ID. If a message logger for the specified logger ID already exists this will
   * be returned to the caller. If no message logger for the specified logger ID
   * currently exists a new logger object will be created and returned.
   * 
   * @param loggerId This is the logger ID which is associated with the requested
   *   message logger. This is a String value which will normally hold the
   *   canonical name of the client class which is using the logger.
   * @param loggerResources This is the name of a {@link java.util.ResourceBundle}
   *   which is to be used for localising messages for this logger. May be null if
   *   none of the messages require localisation.
   * @return Returns a logger object which may be used for logging runtime
   *   messages.
   * @throws MissingResourceException This exception will be thrown if a set of
   *   logger resources have been specified but cannot be found.
   */
  public Logger getLogger(String loggerId, String loggerResources) throws MissingResourceException;

  /**
   * Starts a one-shot timer. This method is used to submit a one-shot timed
   * callback request to the reactor. If the specified timeable object is already
   * associated with a running timer, the timer will be restarted with the new
   * parameters.
   * 
   * @param <T> This type identifier specifies the type of the timer data object
   *   which will be passed as the parameter of the timed callback.
   * @param timeable This is the timeable object which will have its timed
   *   callback executed after the requested delay.
   * @param msDelay This parameter specifies the delay before the timed callback
   *   is to be issued, expressed in milliseconds.
   * @param data This is a timer data object which will be passed back as a
   *   parameter to the timed callback.
   * @throws ReactorNotRunningException This runtime exception will be thrown if
   *   an attempt is made to schedule a timer when the reactor is not running.
   */
  public <T> void runTimerOneShot(Timeable<T> timeable, int msDelay, T data) throws ReactorNotRunningException;

  /**
   * Starts a repeating timer. This method is used to submit a repeating timed
   * callback request to the reactor. If the specified timeable object is already
   * associated with a running timer, the timer will be restarted with the new
   * parameters.
   * 
   * @param <T> This type identifier specifies the type of the timer data object
   *   which will be passed as the parameter of the timed callback.
   * @param timeable This is the timeable object which will have its timed
   *   callback executed at the requested interval.
   * @param msDelay This is the delay before the first timed callback is issued,
   *   specified in milliseconds.
   * @param msInterval This is the interval at which timed callbacks will be
   *   issued, specified in milliseconds. If set to 0 or a negative value, the
   *   timer is treated as a one-shot timer.
   * @param data This is a timer data object which will be passed back as a
   *   parameter to the timed callbacks.
   * @throws ReactorNotRunningException This exception will be thrown if an
   *   attempt is made to schedule a timer when the reactor is not running.
   */
  public <T> void runTimerRepeating(Timeable<T> timeable, int msDelay, int msInterval, T data)
      throws ReactorNotRunningException;

  /**
   * Cancels the timer associated with a given timeable object. There is a
   * one-to-one mapping between timers and their associated timeable objects. By
   * specifying a timeable object as the parameter to this method, the
   * corresponding timer will be cancelled if it is currently running.
   * 
   * @param timeable This is the timeable object for which the associated running
   *   timer should be cancelled.
   */
  public void cancelTimer(Timeable<?> timeable);

  /**
   * Starts executing a threadable task in a new thread. This method submits a
   * threadable task to the reactor to be run in the context of an independent
   * thread. Threads are managed by the reactor using a thread pool, so individual
   * threads will be recycled to execute successive threadable tasks. This means
   * that threadable task objects must not make any assumptions about the thread
   * context in which they are running.
   * 
   * @param <T> This type identifier specifies the data type of the parameter
   *   which will be passed as an input to the threadable task.
   * @param <U> This type identifier specifies the data type of the value which
   *   will be returned by the threadable task.
   * @param threadable This is the threadable task object which is to be executed
   *   in a separate thread.
   * @param data This is the input data object which will be passed to the
   *   {@link Threadable#run(Object) run} method of the threadable task object
   *   when it is called in the context of the new thread.
   * @return Returns a deferred event object which will have its callbacks
   *   executed when the threadable task completes successfully or its errbacks
   *   executed if the threadable task throws an exception.
   * @throws ReactorNotRunningException This exception will be thrown if an
   *   attempt is made to schedule execution of a threadable task object when the
   *   reactor is not running.
   * @throws ThreadableRunningException This runtime exception is thrown when an
   *   attempt is made to schedule a threadable object for execution while it is
   *   already being processed by the reactor. This constitutes a programming
   *   error.
   * 
   */
  public <T, U> Deferred<U> runThread(Threadable<T, U> threadable, T data)
      throws ReactorNotRunningException, ThreadableRunningException;

  /**
   * Starts executing a time limited threadable task in a new thread. This method
   * submits a threadable task to the reactor to be run in the context of an
   * independent thread. Threads are managed by the reactor using a thread pool,
   * so individual threads will be recycled to execute successive threadable
   * tasks. This means that threadable task objects must not make any assumptions
   * about the thread context in which they are running. This method variant
   * includes a timeout parameter which specifies the maximum amount of time for
   * which the threadable task may execute before being automatically cancelled.
   * On cancellation due to a timeout, an exception of type
   * {@link DeferredTimedOutException} is passed back via the errback chain.
   * 
   * @param <T> This type identifier specifies the data type of the parameter
   *   which will be passed as an input to the threadable task.
   * @param <U> This type identifier specifies the data type of the value which
   *   will be returned by the threadable task.
   * @param threadable This is the threadable task object which is to be executed
   *   in a separate thread.
   * @param data This is the input data object which will be passed to the
   *   {@link Threadable#run(Object) run} method of the threadable task object
   *   when it is called in the context of the new thread.
   * @param msTimeout This is the timeout period which is associated with the
   *   threadable task. A threadable task which exceeds this duration will be
   *   automatically cancelled.
   * @return Returns a deferred event object which will have its callbacks
   *   executed when the threadable task completes successfully or its errbacks
   *   executed if the threadable task throws an exception. In the event that
   *   execution of the threadable task times out, the exception passed to the
   *   errback chain will be of type {@link DeferredTimedOutException}.
   * @throws ReactorNotRunningException This exception will be thrown if an
   *   attempt is made to schedule execution of a threadable task object when the
   *   reactor is not running.
   * @throws ThreadableRunningException This runtime exception is thrown when an
   *   attempt is made to schedule a threadable object for execution while it is
   *   already being processed by the reactor. This constitutes a programming
   *   error.
   */
  public <T, U> Deferred<U> runThread(Threadable<T, U> threadable, T data, int msTimeout)
      throws ReactorNotRunningException, ThreadableRunningException;

  /**
   * Cancels a currently executing threadable task. This method is used to request
   * that a currently running threadable task object has its execution cancelled.
   * From the perspective of the threadable task object, cancellation uses the
   * standard thread interruption mechanism.
   * 
   * @param threadable This is the threadable task object for which threaded
   *   execution is to be cancelled.
   */
  public void cancelThread(Threadable<?, ?> threadable);

  /**
   * Creates a new deferred event object. This method is used as a factory for
   * deferred event objects which implement the {@link Deferred} interface.
   * 
   * @param <T> This type identifier specifies the data type of the object which
   *   should be passed as the {@link Deferred#callback(Object) callback}
   *   parameter for the new deferred event object.
   * @return Returns a deferred event object for which the application code is
   *   responsible for issuing callbacks or errbacks.
   */
  public <T> Deferred<T> newDeferred();

  /**
   * Creates a new deferred event object and then issues a callback on it using
   * the supplied callback data.
   * 
   * @param <T> This type identifier specifies the data type of the object which
   *   should be passed as the callback data for the new deferred event object.
   * @param callbackData This is the callback data which is to be passed to the
   *   deferred callback chain.
   * @return Returns a new deferred event object where the callback has already
   *   been issued, passing the supplied callback data to the callback chain.
   */
  public <T> Deferred<T> callDeferred(T callbackData);

  /**
   * Creates a new deferred event object and then issues an error callback on it
   * using the supplied exception condition.
   * 
   * @param <T> This type identifier specifies the data type of the object which
   *   would otherwise have been passed as the callback data for the new deferred
   *   event object.
   * @param error This is the exception condition which is to be passed to the
   *   deferred error callback chain.
   * @return Returns a new deferred event object where the error callback has
   *   already been issued, passing the supplied exception condition to the error
   *   callback chain.
   */
  public <T> Deferred<T> failDeferred(Exception error);

  /**
   * Creates a new deferred callback splitter object. This method is used as a
   * factory for deferred callback splitter objects which implement the
   * {@link DeferredSplitter} interface.
   * 
   * @param <T> This type identifier specifies the type of the callback data
   *   object which will be passed through the deferred splitter.
   * @return Returns a newly created deferred callback splitter object.
   */
  public <T> DeferredSplitter<T> newDeferredSplitter();

  /**
   * Creates a new deferred callback concentrator object. This method is used as a
   * factory for deferred callback concentrator objects which implement the
   * {@link DeferredConcentrator} interface.
   * 
   * @param <T> This type identifier specifies the type of the callback data
   *   object which will be passed through the deferred concentrator.
   * @return Returns a newly created deferred callback concentrator object.
   */
  public <T> DeferredConcentrator<T> newDeferredConcentrator();

  /**
   * Creates a new signal event object. This method is used as a factory for
   * signal event objects which implement the {@link Signal} interface.
   * 
   * @param <T> This type identifier specifies the type of the data object which
   *   will be passed as the parameter to the
   *   {@link Signalable#onSignal(Signal, Object) onSignal} signalable callbacks.
   * @return Returns a new signal event object which can be used by the
   *   application to generate signal events.
   */
  public <T> Signal<T> newSignal();

  /**
   * Gets a handle on the reactor shutdown signal. This method is used to obtain a
   * handle on the reactor shutdown signal event object. This allows application
   * components to register with the signal in order to receive notification that
   * the reactor is shutting down.
   * 
   * @return Returns the signal which is used by the reactor in order to notify
   *   subscribers of reactor shutdown. The signal will pass an integer value as
   *   the data parameter which will be set to zero for normal shutdown.
   */
  public Signal<Integer> getReactorShutdownSignal();

}