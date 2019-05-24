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

/**
 * Defines the deferrable callback interface. Any object which implements this
 * interface is referred to as a <em>deferrable object</em>, implying that it
 * supports deferred callbacks.
 * 
 * @param <T> This type identifier specifies the type of data which may be
 *   passed to the deferrable object via the
 *   {@link #onCallback(Deferred, Object) onCallback} method.
 * @param <U> This type identified specifies the type of data which will be
 *   returned by either the {@link #onCallback(Deferred, Object) onCallback} or
 *   {@link #onErrback(Deferred, Exception) onErrback} method.
 * @author Chris Holgate
 */
public interface Deferrable<T, U> {

  /**
   * This is the callback handler for deferrable objects. It is called in response
   * to a deferred event which indicates successful completion of a given task.
   * 
   * @param deferred This is the deferred event object through which the callback
   *   was issued.
   * @param data This is a data object of type <code>T</code> which encapsulates
   *   any information being passed back as a result of the deferred event.
   * @return Returns a data object of type <code>U</code> which will be propagated
   *   to the next deferrable in the chain as the
   *   {@link #onCallback(Deferred, Object) onCallback} data parameter.
   * @throws Exception Exceptions thrown by callback handlers are propagated to
   *   the next deferrable in the chain as the
   *   {@link #onErrback(Deferred, Exception) onErrback} error parameter.
   */
  public U onCallback(Deferred<T> deferred, T data) throws Exception;

  /**
   * This is the error callback handler for deferrable objects. It is called in
   * response to a deferred error which indicates the failure of a given task.
   * 
   * @param deferred This is the deferred event object through which the error
   *   callback was issued.
   * @param error This is the Exception object which has been caught on task
   *   failure. It contains information about the cause of the error. It can be
   *   either handled locally or re-thrown to pass it up the callback chain.
   * @return Returns a data object of type <code>U</code> which will be propagated
   *   to the next deferrable in the chain as the
   *   {@link #onCallback(Deferred, Object) onCallback} data parameter.
   * @throws Exception Exceptions thrown by error callback handlers are propagated
   *   to the next deferrable in the chain as the
   *   {@link #onErrback(Deferred, Exception) onErrback} error parameter.
   */
  public U onErrback(Deferred<T> deferred, Exception error) throws Exception;

}
