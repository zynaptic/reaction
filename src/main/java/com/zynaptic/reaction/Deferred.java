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

import java.util.function.Function;

/**
 * Defines the deferred event interface. This is the public interface to the
 * <em>deferred event objects</em> implemented by the Reaction framework.
 * Deferred event objects are used to manage callback chains comprising multiple
 * deferrable objects.
 * 
 * @param <T> This type parameter specifies the type of data which will be
 *   emitted by the deferred event object at the end of the current callback
 *   chain.
 * 
 * @author Chris Holgate
 */
public interface Deferred<T> {

  /**
   * Issues a deferred callback. This issues a callback which propagates to the
   * first deferrable object to have been attached to this deferred event. This
   * call is used to notify the attached deferrable objects of successful
   * completion. If this method is called while the reactor is not running, the
   * callback chain will be triggered with an errback encapsulating an exception
   * of type {@link ReactorNotRunningException} instead.
   * 
   * @param data This parameter is a data object of type <code>T</code> which is
   *   used to pass data back to the deferrable object.
   * @throws RestrictedCapabilityException This runtime exception is thrown if an
   *   attempt is made to call this method on a deferred event object reference
   *   with restricted capability.
   * @throws DeferredTriggeredException This runtime exception is raised if the
   *   deferred event has already been triggered via the {@link #callback(Object)
   *   callback} or {@link #errback(Exception) errback} methods.
   */
  public void callback(T data) throws RestrictedCapabilityException, DeferredTriggeredException;

  /**
   * Issues an error callback. This issues an error callback to the first
   * deferrable object to have been attached to this deferred event. This call is
   * used to notify the attached deferrables of error conditions. If this method
   * is called while the reactor is not running, the callback chain will be
   * triggered with an errback encapsulating an exception of type
   * {@link ReactorNotRunningException} instead.
   * 
   * @param error The error parameter passes an exception object which can be used
   *   to identify the error condition.
   * @throws RestrictedCapabilityException This exception is thrown if an attempt
   *   is made to call this method on a deferred event object reference with
   *   restricted capability.
   * @throws DeferredTriggeredException This runtime exception is raised if the
   *   deferred event has already been triggered via the {@link #callback(Object)
   *   callback} or {@link #errback(Exception) errback} methods.
   */
  public void errback(Exception error) throws RestrictedCapabilityException, DeferredTriggeredException;

  /**
   * Attaches a deferrable object to the deferred event. Multiple deferrable
   * objects can be attached, forming a callback chain. The callbacks on
   * deferrable objects are called in the same order in which they are added to
   * the deferred event object. Deferrable objects specify their input and output
   * data types, and the parameterised type of the deferred event object will be
   * updated to match the new output type each time a new deferrable object is
   * added to the callback chain.
   * 
   * @param <U> This type parameter specifies the data type which is returned by
   *   the {@link Deferrable#onCallback(Deferred, Object) onCallback} and
   *   {@link Deferrable#onErrback(Deferred, Exception) onErrback} methods of the
   *   deferrable object which is being added to the callback chain.
   * @param deferrable This is the deferrable object which is to be added to the
   *   callback chain. It may be a link deferrable or a terminal deferrable,
   *   depending on the state of the <code>terminal</code> parameter.
   * @param terminal This flag is set to indicate that this deferrable should
   *   terminate the callback chain. Once a terminal deferrable has been added no
   *   further deferrables can be added.
   * @return Returns a reference to this deferred object where the parameterised
   *   data type has been modified to match the return type declared by the
   *   deferrable object. In order to maintain type consistency, this new
   *   reference should then be used for adding further deferrable objects to the
   *   deferred callback chain.
   * @throws DeferredTerminationException This runtime exception is raised if the
   *   deferred callback chain has already been terminated by adding a terminal
   *   deferrable.
   */
  public <U> Deferred<U> addDeferrable(Deferrable<T, U> deferrable, boolean terminal)
      throws DeferredTerminationException;

  /**
   * Attaches a deferrable object to the deferred event. Multiple deferrable
   * objects can be attached, forming a callback chain. The callbacks on
   * deferrable objects are called in the same order in which they are added to
   * the deferred event object. Deferrable objects specify their input and output
   * data types, and the parameterised type of the deferred event object will be
   * updated to match the new output type each time a new deferrable object is
   * added to the callback chain.
   * 
   * @param <U> This type parameter specifies the data type which is returned by
   *   the {@link Deferrable#onCallback(Deferred, Object) onCallback} and
   *   {@link Deferrable#onErrback(Deferred, Exception) onErrback} methods of the
   *   deferrable object which is being added to the callback chain.
   * @param deferrable This is the deferrable object which is to be added to the
   *   callback chain.
   * @return Returns a reference to this deferred object where the parameterised
   *   data type has been modified to match the return type declared by the
   *   deferrable object. In order to maintain type consistency, this new
   *   reference should then be used for adding further deferrable objects to the
   *   deferred callback chain.
   * @throws DeferredTerminationException This runtime exception is raised if the
   *   deferred callback chain has already been terminated by adding a terminal
   *   deferrable.
   */
  public <U> Deferred<U> addDeferrable(Deferrable<T, U> deferrable) throws DeferredTerminationException;

  /**
   * Attaches a callback function to the deferred event. This will be wrapped as a
   * deferrable object where the specified function is used as the
   * {@link Deferrable#onCallback} method and a pass-through function is used as
   * the {@link Deferrable#onErrback} method.
   * 
   * @param <U> This type parameter specifies the data type which is returned by
   *   the callback function.
   * @param callback This is the lambda function which is to be used as the
   *   attached callback function.
   * @return Returns a reference to this deferred object where the parameterised
   *   data type has been modified to match the return type declared by the lambda
   *   function. In order to maintain type consistency, this new reference should
   *   then be used for adding further deferrable objects to the deferred callback
   *   chain.
   * @throws DeferredTerminationException This runtime exception is raised if the
   *   deferred callback chain has already been terminated by adding a terminal
   *   deferrable.
   */
  public <U> Deferred<U> addCallback(Function<T, U> callback) throws DeferredTerminationException;

  /**
   * Attaches an error callback function to the deferred event. This will be
   * wrapped as a deferrable object where the specified function is used as the
   * {@link Deferrable#onErrback} method and a pass-through function is used as
   * the {@link Deferrable#onCallback} method.
   * 
   * @param errback This is the lambda function which is to be used as the
   *   attached error callback function.
   * @return Returns a reference to this deferred object where the parameterised
   *   data type is preserved, reflecting the fact that the callback method is a
   *   pass-through function.
   * @throws DeferredTerminationException This runtime exception is raised if the
   *   deferred callback chain has already been terminated by adding a terminal
   *   deferrable.
   */
  public Deferred<T> addErrback(Function<Exception, T> errback) throws DeferredTerminationException;

  /**
   * Terminates a deferred callback chain. This method is used to terminate a
   * deferred callback chain without adding any further deferrable callback
   * objects.
   * 
   * @return Returns a reference to the current {@link Deferred} interface,
   *   allowing fluent-style callback chain termination.
   * @throws DeferredTerminationException This runtime exception is raised if the
   *   deferred callback chain has already been terminated, or if no deferrable
   *   callback handlers have previously been added to the callback chain.
   */
  public Deferred<T> terminate() throws DeferredTerminationException;

  /**
   * Sets the timeout associated with the deferred event. The timeout is scheduled
   * for the specified number of milliseconds after this call was made. If the
   * timeout expires before the deferred event has been triggered it generates an
   * errback call, passing an exception of type {@link DeferredTimedOutException}
   * as the parameter. By default no timeout is set. If this function is called
   * multiple times, the most recently requested timeout is used. A timeout value
   * of zero or less may be used to force an immediate timeout.
   * 
   * @param msTimeout This is the timeout to be used by the deferred event,
   *   specified as an integer number of milliseconds.
   * @return Returns a reference to the current {@link Deferred} interface,
   *   allowing fluent-style timeout assignments.
   * @throws ReactorNotRunningException This exception is thrown if an attempt is
   *   made to set a deferred timeout when the reactor is not running.
   */
  public Deferred<T> setTimeout(int msTimeout) throws ReactorNotRunningException;

  /**
   * Cancels the timeout associated with the deferred event. If there is an
   * outstanding timeout it will be cancelled, otherwise this call has no effect.
   * 
   * @return Returns a reference to the current {@link Deferred} interface,
   *   allowing fluent-style timeout assignments.
   */
  public Deferred<T> cancelTimeout();

  /**
   * Discards the deferred event. There are circumstances where an API call may
   * return a deferred event for which the caller has no use. Ignoring the
   * deferred results in an unterminated deferred going out of scope, which is
   * reported in the logs as an error. This method terminates the deferred with a
   * default deferrable which simply discards any callbacks and reports any
   * errback conditions to the log.
   * 
   * @throws DeferredTerminationException This runtime exception is raised if the
   *   deferred callback chain has already been terminated by adding a terminal
   *   deferrable.
   */
  public void discard() throws DeferredTerminationException;

  /**
   * Convert the deferred interface to restricted capability. This method should
   * be used to restrict the capability of a deferred interface so that the
   * {@link #callback(Object) callback} and {@link #errback(Exception) errback}
   * methods are protected from unauthorised use.
   * 
   * @return Returns a version of the current {@link Deferred} interface with
   *   restricted capability.
   */
  public Deferred<T> makeRestricted();

  /**
   * Performs a synchronous wait for the deferred callback results. This method
   * causes the current thread to block until the deferred event has been
   * triggered and the callback results passed back. It must not be called from
   * within the main reactor thread context (ie, not from within any timed,
   * signalled or deferred callback handlers).
   * 
   * @return Returns the callback parameter passed back via the deferred callback
   *   chain.
   * @throws DeferredContextException This exception will be thrown if an attempt
   *   is made to call this method from within the context of the main reactor
   *   thread.
   * @throws Exception This arbitrary exception will be thrown if an error
   *   condition has been passed back via the deferred errback chain. The types of
   *   exception which may be thrown are dependent on the error conditions which
   *   may occur for a given application call.
   */
  public T defer() throws DeferredContextException, Exception;

}
