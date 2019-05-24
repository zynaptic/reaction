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
 * Defines a <em>splitter</em> for deferred event callback chains. This allows a
 * single deferred event to trigger multiple parallel callback chains. The data
 * object passed into the splitter will be passed up to each of the parallel
 * callback chains in strict sequence - observing the ordering with which the
 * deferrable callbacks were added to the deferred splitter object. Callback
 * chains should not usually make any change to the state of the data parameter
 * object. Therefore, when using callback chain splitters it is good practice to
 * make the data parameter object immutable in order to enforce the correct
 * behaviour.
 * 
 * @param <T> This type parameter specifies the type of data which may be passed
 *   into the deferred splitter by the input deferred event object.
 * 
 * @author Chris Holgate
 */
public interface DeferredSplitter<T> {

  /**
   * Attaches the input deferred event object. This method is used to attach the
   * input deferred event object which will be used to trigger the callbacks in
   * the deferred callback splitter. The callback chain associated with the input
   * deferred event object will automatically be terminated by the splitter. This
   * method should only be called once for each splitter object.
   * 
   * @param deferred This is the input deferred event object which will be used to
   *   trigger the callbacks on the splitter.
   * @throws DeferredTerminationException This exception will be thrown if an
   *   attempt is made to add more than one deferrable event object as an input.
   *   It will also be generated if the callback chain associated with the
   *   deferred event object has already been terminated.
   */
  public void addInputDeferred(Deferred<T> deferred) throws DeferredTerminationException;

  /**
   * Obtains a handle on a unique output deferred event object. This method is
   * used to obtain a handle on a unique output deferred event object within the
   * callback chain splitter. Each time this method is called a new object
   * implementing the {@link Deferred} interface is added to the splitter and
   * returned to the caller.
   * 
   * @return Returns a deferred event object which will have its callbacks or
   *   errbacks executed when the splitter is triggered by its input callback
   *   chain.
   */
  public Deferred<T> getOutputDeferred();

}
