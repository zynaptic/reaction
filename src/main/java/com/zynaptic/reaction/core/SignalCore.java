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

import com.zynaptic.reaction.ReactorNotRunningException;
import com.zynaptic.reaction.RestrictedCapabilityException;
import com.zynaptic.reaction.Signal;
import com.zynaptic.reaction.SignalContextException;
import com.zynaptic.reaction.Signalable;

/**
 * This class provides the implementation of the {@link Signal} API.
 * 
 * @author Chris Holgate
 */
public final class SignalCore<T> implements Signal<T> {

  // Local handle on the reactor.
  private final ReactorCore reactorCore = ReactorCore.getReactorCore();

  // List of signalable objects associated with this signal.
  private final LinkedList<SignalableWrapper<T>> signalableList = new LinkedList<SignalableWrapper<T>>();

  // Flag used to prevent addition and removal of signalable objects during
  // callbacks.
  private boolean callbacksRunning = false;

  /*
   * Implements Signal.subscribe(...)
   */
  public void subscribe(final Signalable<T> signalable) throws SignalContextException {
    subscribe(signalable, 0);
  }

  /*
   * Implements Signal.subscribe(...)
   */
  public synchronized void subscribe(final Signalable<T> signalable, int priorityLevel) throws SignalContextException {
    if (callbacksRunning == false) {
      removeListEntry(signalable);
      addListEntry(signalable, priorityLevel);
    } else {
      throw new SignalContextException("Attempted to subscribe signalable from signal callback.");
    }
  }

  /*
   * Implements Signal.unsubscribe(...)
   */
  public synchronized void unsubscribe(final Signalable<T> signalable) throws SignalContextException {
    if (callbacksRunning == false) {
      removeListEntry(signalable);
    } else {
      throw new SignalContextException("Attempted to unsubscribe signalable from signal callback.");
    }
  }

  /*
   * Delegates Signal.signal(...)
   */
  public void signal(final T data) throws ReactorNotRunningException {
    reactorCore.signal(this, data, false);
  }

  /*
   * Delegates Signal.signalFinalize(...)
   */
  public void signalFinalize(final T data) throws ReactorNotRunningException {
    reactorCore.signal(this, data, true);
  }

  /*
   * Implements Signal.makeRestricted()
   */
  public Signal<T> makeRestricted() {
    return new RestrictedSignalCore(this);
  }

  /*
   * Provide equivalence testing between normal and restricted signal object
   * references.
   */
  @Override
  public boolean equals(Object signal) {
    if (signal instanceof SignalCore.RestrictedSignalCore) {
      return signal.equals(this);
    } else {
      return super.equals(signal);
    }
  }

  /*
   * Provides equivalent hash code calculation between normal and restricted
   * signal object.
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /*
   * Processes a signal event by forwarding the specified data to all subscribed
   * signalable objects.
   */
  synchronized void processSignal(final T data, final boolean finalize) {
    callbacksRunning = true;

    // Issue callbacks to all subscribed signalable objects.
    Iterator<SignalableWrapper<T>> sigIter = signalableList.iterator();
    while (sigIter.hasNext()) {
      sigIter.next().getSignalable().onSignal(this.makeRestricted(), data);
    }

    // Remove all signalable objects when finalized.
    if (finalize) {
      signalableList.clear();
    }
    callbacksRunning = false;
  }

  /*
   * Inserts a signalable object into the subscriber list at the appropriate
   * priority level position.
   */
  private void addListEntry(Signalable<T> signalable, int priorityLevel) {
    SignalableWrapper<T> signalableWrapper = new SignalableWrapper<T>(signalable, priorityLevel);
    int i;
    int listSize = signalableList.size();
    for (i = 0; i < listSize; i++) {
      if (priorityLevel >= signalableList.get(i).getPriority()) {
        signalableList.add(i, signalableWrapper);
        break;
      }
    }
    if (i == listSize) {
      signalableList.addLast(signalableWrapper);
    }
  }

  /*
   * Removes a signalable object from the subscriber list, if present. Requires a
   * linear search through the list.
   */
  private void removeListEntry(Signalable<T> signalable) {
    for (int i = 0; i < signalableList.size(); i++) {
      if (signalableList.get(i).getSignalable().equals(signalable)) {
        signalableList.remove(i);
      }
    }
  }

  /*
   * Implements a wrapper around a signalable object which includes the associated
   * priority level.
   */
  private final class SignalableWrapper<U> {
    private final Signalable<U> signalable;
    private final int priority;

    private SignalableWrapper(Signalable<U> signalable, int priority) {
      this.signalable = signalable;
      this.priority = priority;
    }

    private Signalable<U> getSignalable() {
      return signalable;
    }

    private int getPriority() {
      return priority;
    }
  }

  /*
   * Implement restricted interface wrapper for signal objects.
   */
  private final class RestrictedSignalCore implements Signal<T> {
    private final SignalCore<T> signalCore;

    private RestrictedSignalCore(SignalCore<T> signalCore) {
      this.signalCore = signalCore;
    }

    public void subscribe(Signalable<T> signalable) {
      signalCore.subscribe(signalable);
    }

    public void subscribe(Signalable<T> signalable, int priorityLevel) {
      signalCore.subscribe(signalable, priorityLevel);
    }

    public void unsubscribe(Signalable<T> signalable) {
      signalCore.unsubscribe(signalable);
    }

    public void signal(T data) throws RestrictedCapabilityException {
      throw new RestrictedCapabilityException("Method not available for restricted Signal interface.");
    }

    public void signalFinalize(T data) throws RestrictedCapabilityException {
      throw new RestrictedCapabilityException("Method not available for restricted Signal interface.");
    }

    public Signal<T> makeRestricted() {
      return this;
    }

    @Override
    public boolean equals(Object signal) {
      return signalCore.equals(signal);
    }

    @Override
    public int hashCode() {
      return signalCore.hashCode();
    }
  }
}
