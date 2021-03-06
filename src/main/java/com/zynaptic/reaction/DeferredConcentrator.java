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

import java.util.List;

/**
 * Defines a <em>concentrator</em> for deferred events. This allows the results
 * of multiple deferred event callbacks to be assembled into a list and then
 * passed up a single output callback chain. A list is assembled by adding input
 * deferred event objects, then requesting the output deferred event
 * object&nbsp;- which implies that the input list is complete.
 * <p>
 * A callback will be generated by the output deferred event only if all input
 * deferred events completed successfully. In this case, the callback parameter
 * will be an array list containing the callback data returned by each of the
 * input callback chains, listed according to the order in which the deferred
 * event objects were added.
 * <p>
 * If an errback is encountered on any of the input deferred events, it will be
 * propagated to the output callback chain and all other results will be
 * discarded. Only the first errback condition to be generated will be passed
 * back in this way.
 * 
 * @param <T> This type parameter identifies the type of the data object which
 *   will be passed into the deferred concentrator via the input deferred event
 *   objects.
 * 
 * @author Chris Holgate
 */
public interface DeferredConcentrator<T> {

  /**
   * Adds an input deferred event object to the concentrator list. The callback
   * chain associated with the deferred event object will automatically be
   * terminated.
   * 
   * @param deferred This is the deferred event object which is to be added to the
   *   concentrator list.
   * @return Returns a reference to the deferred concentrator instance allowing
   *   fluent-style application of the method.
   * @throws DeferredTerminationException This exception is thrown if the deferred
   *   event object being passed as the parameter has already had its callback
   *   chain terminated, or the output deferred event object has already been
   *   requested which prevents further additions to the list.
   */
  public DeferredConcentrator<T> addInputDeferred(Deferred<T> deferred) throws DeferredTerminationException;

  /**
   * Accesses the output deferred event object for the concentrator list. The
   * deferred event object which is returned will have its callbacks triggered on
   * completion of all the deferred events held in the list or its errbacks
   * triggered if one of the deferred input events generates an errback. This
   * should only be called once all the input deferred event objects have been
   * added to the concentrator list.
   * 
   * @return Returns the deferred event object which will have its callbacks
   *   executed on completion of all the deferred input events.
   */
  public Deferred<List<T>> getOutputDeferred();

}
