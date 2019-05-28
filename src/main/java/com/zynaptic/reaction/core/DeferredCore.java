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

import java.util.Date;
import java.util.LinkedList;

import com.zynaptic.reaction.Deferrable;
import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.DeferredContextException;
import com.zynaptic.reaction.DeferredTerminationException;
import com.zynaptic.reaction.DeferredTimedOutException;
import com.zynaptic.reaction.DeferredTriggeredException;
import com.zynaptic.reaction.ReactorNotRunningException;
import com.zynaptic.reaction.RestrictedCapabilityException;
import com.zynaptic.reaction.Timeable;

/**
 * This class provides the core implementation of the {@link Deferred} API.
 * Generic type checking is carried out on the public interface, so processing
 * within the scope of this core implementation can make use of unchecked type
 * references.
 * 
 * @author Chris Holgate
 */
public final class DeferredCore<T> implements Deferred<T>, Timeable<T> {

  // Deferred state flags.
  private boolean callbackDataValid = false;
  private boolean callbackErrorValid = false;
  private boolean callbackChainTerminated = false;
  private boolean ignoreCallback = false;

  // Stack context used for debugging deferred event objects when they go out of
  // scope without triggering.
  private final long stackTraceTimestamp;
  private final StackTraceElement[] stackTraceElements;

  // Callback chain data.
  private Object callbackData = null;
  private Exception callbackError = null;
  private final LinkedList<Deferrable<?, ?>> callbackChain = new LinkedList<Deferrable<?, ?>>();

  // Local handle on the reactor.
  private final ReactorCore reactorCore = ReactorCore.getReactorCore();

  /**
   * The default constructor sets the stack context under which the deferred event
   * was created. This allows exceptions due to untriggered deferred events going
   * out of scope to return meaningful debug information.
   */
  public DeferredCore() {
    stackTraceTimestamp = System.currentTimeMillis();
    stackTraceElements = Thread.currentThread().getStackTrace();
  }

  /**
   * Tidy up the deferred event object when it is garbage collected. This adds
   * warnings to the logs if the deferred goes out of scope without being
   * triggered or terminated. Currently uses the deprecated 'finalize' method.
   */
  @SuppressWarnings("deprecation")
  @Override
  public void finalize() {
    if ((!callbackDataValid) && (!callbackErrorValid)) {
      callbackError = new DeferredTimedOutException(
          "Deferred created " + new Date(stackTraceTimestamp).toString() + " : going out of scope (untriggered).");
      callbackError.setStackTrace(stackTraceElements);
      reactorCore.closeDeferred(this, callbackError);
    } else if (!callbackChainTerminated) {
      callbackError = new DeferredTerminationException(
          "Deferred created " + new Date(stackTraceTimestamp).toString() + " : going out of scope (unterminated).");
      callbackError.setStackTrace(stackTraceElements);
      reactorCore.closeDeferred(this, callbackError);
    }
  }

  /*
   * Implements Deferred.addDeferrable(...)
   */
  public synchronized <U> Deferred<U> addDeferrable(final Deferrable<T, U> deferrable, final boolean terminal)
      throws DeferredTerminationException {

    // Check to see if the callback chain has been terminated.
    if (callbackChainTerminated) {
      throw new DeferredTerminationException("Deferred : callback chain already terminated.");
    }

    // Append deferrable to the callback chain.
    callbackChain.addLast(deferrable);

    // Mark callback chain as terminated and process it if possible.
    if (terminal) {
      callbackChainTerminated = true;
      if ((callbackDataValid) || (callbackErrorValid)) {
        reactorCore.processDeferred(this);
      }
    }
    return this.typePunned();
  }

  /*
   * Implements Deferred.addDeferrable(...)
   */
  public synchronized <U> Deferred<U> addDeferrable(final Deferrable<T, U> deferrable)
      throws DeferredTerminationException {

    // Check to see if the callback chain has been terminated.
    if (callbackChainTerminated) {
      throw new DeferredTerminationException("Deferred : callback chain already terminated.");
    }

    // Append deferrable to the callback chain.
    callbackChain.addLast(deferrable);
    return this.typePunned();
  }

  /*
   * Implements Deferred.terminate()
   */
  public synchronized Deferred<T> terminate() {

    // Check to see if the callback chain has been terminated.
    if (callbackChainTerminated) {
      throw new DeferredTerminationException("Deferred : callback chain already terminated.");
    }

    // Mark callback chain as terminated and process it if possible.
    callbackChainTerminated = true;
    if ((callbackDataValid) || (callbackErrorValid)) {
      reactorCore.processDeferred(this);
    }
    return this;
  }

  /*
   * Implements Deferred.callback(...)
   */
  public synchronized void callback(final T data) throws DeferredTriggeredException {

    // Discard the callback after a timeout.
    if (ignoreCallback) {
      ignoreCallback = false;
    }

    // Check to see if this deferred event has already been triggered.
    else if ((callbackDataValid) || (callbackErrorValid)) {
      throw new DeferredTriggeredException("Deferred : already triggered.");
    }

    // Cache the new data parameter and process the callback chain.
    else {
      callbackData = data;
      callbackDataValid = true;
      reactorCore.cancelTimer(this);
      if (callbackChainTerminated) {
        reactorCore.processDeferred(this);
      }
    }
  }

  /*
   * Implements Deferred.errback(...)
   */
  public synchronized void errback(final Exception error) throws DeferredTriggeredException {

    // Discard the errback after a timeout.
    if (ignoreCallback) {
      ignoreCallback = false;
    }

    // Check to see if this deferred event has already been triggered.
    else if ((callbackDataValid) || (callbackErrorValid)) {
      throw new DeferredTriggeredException("Deferred : already triggered.");
    }

    // Cache the new error parameter and process the callback chain.
    else {
      callbackError = error;
      callbackErrorValid = true;
      reactorCore.cancelTimer(this);
      if (callbackChainTerminated) {
        reactorCore.processDeferred(this);
      }
    }
  }

  /*
   * Implements Deferred.defer()
   */
  public T defer() throws DeferredContextException, Exception {
    reactorCore.checkDeferredContext();
    WakeupHandler wakeupHandler = new WakeupHandler();
    this.addDeferrable(wakeupHandler, true);
    return wakeupHandler.waitForCallback();
  }

  /*
   * Implements Deferred.setTimeout(...)
   */
  public Deferred<T> setTimeout(final int msTimeout) throws ReactorNotRunningException {
    reactorCore.runTimerOneShot(this, msTimeout, null);
    return this;
  }

  /*
   * Implements Deferred.cancelTimeout(...)
   */
  public Deferred<T> cancelTimeout() {
    reactorCore.cancelTimer(this);
    return this;
  }

  /*
   * Implements Deferred.discard()
   */
  public void discard() {
    this.terminate();
  }

  /*
   * Implements Deferred.makeRestricted()
   */
  public Deferred<T> makeRestricted() {
    return new RestrictedDeferredCore(this);
  }

  /*
   * Provide equivalence testing between normal and restricted deferred object
   * references.
   */
  @Override
  public boolean equals(Object deferred) {
    if (deferred instanceof DeferredCore.RestrictedDeferredCore) {
      return deferred.equals(this);
    } else {
      return super.equals(deferred);
    }
  }

  /*
   * Provide hash code equivalence testing between normal and restricted deferred
   * object references.
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /*
   * Implements Timeable.tick(...)
   */
  public synchronized void onTick(final T data) {

    // If not already triggered, queue the timeout errback and discard
    // future callback and errback requests.
    if ((!callbackDataValid) && (!callbackErrorValid)) {
      ignoreCallback = true;
      callbackError = new DeferredTimedOutException("Timeout in deferred.");
      callbackErrorValid = true;
      if (callbackChainTerminated) {
        reactorCore.processDeferred(this);
      }
    }
  }

  /*
   * Process callback chain, calling all queued callbacks in sequence. This will
   * only be triggered once a terminal deferrable has been added and an event is
   * ready for processing. Note that deferrable generic types will have been
   * discarded before the callback chain is processed, so raw deferrable
   * references are used within the scope of this function.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  synchronized void processCallbackChain(final boolean reactorRunning) {

    // Deal with callbacks if reactor is not running.
    if (!reactorRunning) {
      callbackError = new ReactorNotRunningException("Can't schedule deferred callbacks when reactor not running.");
      callbackErrorValid = true;
    }

    // Propagate the callbacks.
    while (!callbackChain.isEmpty()) {
      Deferrable deferrable = callbackChain.removeFirst();
      try {

        // Issue error callback for pending errors.
        if (callbackErrorValid) {
          callbackData = deferrable.onErrback(this.makeRestricted(), callbackError);
        }

        // Issue standard callback for pending data.
        else if (callbackDataValid) {
          callbackData = deferrable.onCallback(this.makeRestricted(), callbackData);
        }
        callbackErrorValid = false;
        callbackDataValid = true;
      }

      // Exceptions thrown during callback processing are stored for forwarding
      // to the next deferrable in the list. Errors are left to propagate back
      // to the reactor.
      catch (Exception err) {
        callbackError = err;
        callbackErrorValid = true;
        callbackDataValid = false;
      }
    }

    // Any data returned by the terminal deferrable is discarded.
    // Terminal error conditions are passed to the reactor on closing.
    reactorCore.closeDeferred(this, callbackErrorValid ? callbackError : null);
  }

  /*
   * Provide type punning function for reassigning the generic type of a given
   * deferred event object.
   */
  @SuppressWarnings("unchecked")
  private <PT> Deferred<PT> typePunned() {
    return (Deferred<PT>) this;
  }

  /*
   * Provides a wakeup callback handler for reactivating a thread after a deferred
   * callback has been issued. The monitor on this object is used for wakup
   * notification. The callback results are cached and then the waiting thread is
   * interrupted in order to continue processing. The cached results are then
   * returned to the original caller. If the thread is interrupted from elsewhere,
   * the InterruptedException is ignored until the callback has completed.
   */
  private final class WakeupHandler implements Deferrable<T, T> {
    private T callbackData = null;
    private Exception callbackError = null;
    private boolean callbackComplete = false;
    private boolean errbackComplete = false;

    private synchronized T waitForCallback() throws Exception {
      while ((!callbackComplete) && (!errbackComplete)) {
        try {
          this.wait();
        } catch (InterruptedException interruptedException) {
          // Ignore and continue waiting.
        }
      }
      if (callbackComplete) {
        return callbackData;
      } else {
        throw callbackError;
      }
    }

    public synchronized T onCallback(Deferred<T> deferred, T data) {
      callbackData = data;
      callbackComplete = true;
      this.notifyAll();
      return null;
    }

    public synchronized T onErrback(Deferred<T> deferred, Exception error) {
      callbackError = error;
      errbackComplete = true;
      this.notifyAll();
      return null;
    }
  }

  /*
   * Implement restricted interface wrapper for deferred objects.
   */
  private final class RestrictedDeferredCore implements Deferred<T> {
    private final DeferredCore<T> deferredCore;

    private RestrictedDeferredCore(DeferredCore<T> deferredCore) {
      this.deferredCore = deferredCore;
    }

    public void callback(T data) throws RestrictedCapabilityException {
      throw new RestrictedCapabilityException("Method not available for restricted Deferred interface.");
    }

    public void errback(Exception error) throws RestrictedCapabilityException {
      throw new RestrictedCapabilityException("Method not available for restricted Deferred interface.");
    }

    public T defer() throws DeferredContextException, Exception {
      return deferredCore.defer();
    }

    public <U> Deferred<U> addDeferrable(Deferrable<T, U> deferrable, boolean terminal)
        throws DeferredTerminationException {
      return deferredCore.addDeferrable(deferrable, terminal);
    }

    public <U> Deferred<U> addDeferrable(Deferrable<T, U> deferrable) throws DeferredTerminationException {
      return deferredCore.addDeferrable(deferrable);
    }

    public Deferred<T> terminate() throws DeferredTerminationException {
      return deferredCore.terminate();
    }

    public Deferred<T> setTimeout(int msTimeout) throws ReactorNotRunningException {
      deferredCore.setTimeout(msTimeout);
      return this;
    }

    public Deferred<T> cancelTimeout() {
      deferredCore.cancelTimeout();
      return this;
    }

    public void discard() {
      deferredCore.discard();
    }

    public Deferred<T> makeRestricted() {
      return this;
    }

    @Override
    public boolean equals(Object deferred) {
      return deferredCore.equals(deferred);
    }

    @Override
    public int hashCode() {
      return deferredCore.hashCode();
    }
  }
}
