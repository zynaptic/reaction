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

import java.util.Iterator;
import java.util.LinkedList;

import com.zynaptic.reaction.Deferrable;
import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.DeferredSplitter;
import com.zynaptic.reaction.DeferredTerminationException;
import com.zynaptic.reaction.DeferredTriggeredException;

/**
 * This class provides the core implementation of the {@link DeferredSplitter}
 * API.
 * 
 * @author Chris Holgate
 */
public final class DeferredSplitterCore<T> implements DeferredSplitter<T>, Deferrable<T, T> {

  // Deferred state flags.
  private boolean callbackDataValid = false;
  private boolean callbackErrorValid = false;

  // Callback chain data.
  private T callbackData = null;
  private Exception callbackError = null;
  private LinkedList<Deferred<T>> callbackList = new LinkedList<Deferred<T>>();

  // Handle on the input deferred event object.
  private Deferred<T> deferredInput = null;

  // Local handle on the reactor.
  private final ReactorCore reactorCore = ReactorCore.getReactorCore();

  /*
   * Attach a single deferred event input to this deferrable list.
   */
  public synchronized DeferredSplitter<T> addInputDeferred(final Deferred<T> deferred)
      throws DeferredTerminationException {

    // Only a single deferred input event can be attached.
    if (deferredInput != null) {
      throw new DeferredTerminationException("Input deferred event object already registered with deferred splitter.");
    }

    // Register this object as the terminal deferrable for the deferred
    // input event.
    deferredInput = deferred;
    deferredInput.addDeferrable(this, true);
    return this;
  }

  /*
   * Add a new deferred to the list of callback chains and return its handle.
   */
  public synchronized Deferred<T> getOutputDeferred() {
    Deferred<T> newDeferred = reactorCore.newDeferred();

    // If the input callback has already been triggered, just forward it to
    // a newly registered deferred object.
    if (callbackDataValid) {
      newDeferred.callback(callbackData);
    }

    // If the input errback has already been triggered, just forward it to a
    // newly registered deferred object.
    else if (callbackErrorValid) {
      newDeferred.errback(callbackError);
    }

    // Input callback has not yet been triggered, so create a new deferred
    // object and add it to the callback list.
    else {
      callbackList.add(newDeferred);
    }
    return newDeferred.makeRestricted();
  }

  /*
   * Cache the callback data object and forward it to all entries in the list of
   * callback chains.
   */
  public synchronized T onCallback(final Deferred<T> deferred, final T data) throws Exception {

    // Trap multiple callbacks.
    if (callbackDataValid || callbackErrorValid) {
      throw new DeferredTriggeredException("DeferredSplitter : already triggered.");
    }

    // Forward the callback data to each deferred callback in the list.
    callbackDataValid = true;
    callbackData = data;
    Iterator<Deferred<T>> listIter = callbackList.iterator();
    for (int i = 0; i < callbackList.size(); i++) {
      (listIter.next()).callback(callbackData);
    }
    callbackList = null;
    return null;
  }

  /*
   * Cache the errback exception and forward it to all entries in the list of
   * callback chains.
   */
  public synchronized T onErrback(final Deferred<T> deferred, final Exception error) throws Exception {

    // Trap multiple callbacks.
    if (callbackDataValid || callbackErrorValid) {
      throw new DeferredTriggeredException("DeferredSplitter : already triggered.");
    }

    // Forward the errback exception to each deferred errback in the list.
    callbackErrorValid = true;
    callbackError = error;
    Iterator<Deferred<T>> listIter = callbackList.iterator();
    for (int i = 0; i < callbackList.size(); i++) {
      (listIter.next()).errback(callbackError);
    }
    callbackList = null;
    return null;
  }
}
