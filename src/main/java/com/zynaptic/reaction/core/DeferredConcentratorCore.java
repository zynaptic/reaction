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

package com.zynaptic.reaction.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.zynaptic.reaction.Deferrable;
import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.DeferredConcentrator;
import com.zynaptic.reaction.DeferredTerminationException;

/**
 * This class provides the concrete implementation of the
 * {@link DeferredConcentrator} API.
 * 
 * @author Chris Holgate
 * 
 */
public final class DeferredConcentratorCore<T> implements DeferredConcentrator<T>, Deferrable<T, T> {

  // The deferred event object used for output.
  private Deferred<List<T>> deferredOutput = null;

  // Map used to associate input deferred events to result array positions.
  private int resultIndex = 0;
  private final HashMap<Deferred<T>, Integer> resultIndexMap = new HashMap<Deferred<T>, Integer>();

  // Cache the input callback data.
  private Exception callbackError = null;
  private final ArrayList<T> callbackData = new ArrayList<T>();

  // Local handle on the reactor.
  private final ReactorCore reactorCore = ReactorCore.getReactorCore();

  /*
   * Attach multiple input events to the deferred list.
   */
  public synchronized void addInputDeferred(final Deferred<T> deferred) throws DeferredTerminationException {

    // Check to see if the output deferred has already been requested.
    if (deferredOutput != null) {
      throw new DeferredTerminationException("No more deferred concentrator inputs can be added.");
    }

    // Add the deferred input to the result index map.
    resultIndexMap.put(deferred, Integer.valueOf(resultIndex++));

    // Terminate the deferred input callback chain.
    deferred.addDeferrable(this, true);
  }

  /*
   * Get a handle on the output deferred event object.
   */
  public synchronized Deferred<List<T>> getOutputDeferred() {

    // Create the deferred output event if required. Issue the callbacks if
    // ready.
    if (deferredOutput == null) {
      callbackData.trimToSize();
      deferredOutput = reactorCore.newDeferred();
      if (callbackError != null) {
        deferredOutput.errback(callbackError);
      } else if (resultIndexMap.size() == 0) {
        deferredOutput.callback(callbackData);
      }
    }
    return deferredOutput.makeRestricted();
  }

  /*
   * Handle callbacks from input callback chains.
   */
  public synchronized T onCallback(final Deferred<T> deferred, final T data) {

    // Retrieve the result index from the map.
    int resultIndex = (resultIndexMap.remove(deferred)).intValue();

    // Extend the array if required.
    while (callbackData.size() <= resultIndex) {
      callbackData.add(null);
    }

    // Insert the result into the results array.
    callbackData.set(resultIndex, data);

    // Generate the output callback if all input callbacks have completed.
    if ((deferredOutput != null) && (resultIndexMap.size() == 0)) {
      deferredOutput.callback(callbackData);
    }
    return null;
  }

  /*
   * Handle errbacks from input callback chains.
   */
  public synchronized T onErrback(final Deferred<T> deferred, final Exception error) {
    if (callbackError == null) {
      callbackError = error;
      if (deferredOutput != null) {
        deferredOutput.errback(callbackError);
      }
    }
    return null;
  }
}
