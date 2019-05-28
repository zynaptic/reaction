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

package com.zynaptic.reaction.test.core;

import com.zynaptic.reaction.DeferredTimedOutException;
import com.zynaptic.reaction.DeferredTriggeredException;
import com.zynaptic.reaction.Deferrable;
import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.Reactor;
import com.zynaptic.reaction.Timeable;

/**
 * This aggregation class contains all the tests required to verify the deferred
 * timeout behaviour.
 * 
 * @author Chris Holgate
 */
public class DeferredTimeoutTests {

  // Define acceptable error when doing timing checks.
  private static final int MAX_TIMING_ERROR = 250;

  /**
   * Test deferred timeouts (normal operation). Legacy test harness does not
   * include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class BasicDeferredTimeout implements Deferrable, Timeable {

    private Reactor reactor;
    private Deferred deferredDone;
    private Deferred deferredLocal;
    private long timeStamp;

    /*
     * Timeable tick function is only called on reactor startup.
     */
    public void onTick(Object data) {
      System.out.println("Deferred : Testing basic deferred timeouts.");
      Object[] params = (Object[]) data;
      reactor = (Reactor) params[0];
      deferredDone = (Deferred) params[1];
      timeStamp = System.currentTimeMillis();
      deferredLocal = reactor.newDeferred();
      deferredLocal.addDeferrable(this, true);
      try {
        deferredLocal.setTimeout(1000);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }

    /*
     * Deferrable callback should never occur.
     */
    public Object onCallback(Deferred deferred, Object data) {
      deferredDone.errback(new Exception("Unexpected callback."));
      return null;
    }

    /*
     * Deferrable errback should occur with timeout exception.
     */
    public Object onErrback(Deferred deferred, Exception error) {
      System.out.println("  Checking errback conditions.");

      // Check correct callback.
      long timeError = System.currentTimeMillis() - timeStamp - 1000;
      if ((timeError > MAX_TIMING_ERROR) && (timeError < -MAX_TIMING_ERROR)) {
        deferredDone.errback(new Exception("Deferred timeout timing error."));
        return null;
      }
      if (!(error instanceof DeferredTimedOutException)) {
        deferredDone.errback(new Exception("Unexpected exception type."));
        return null;
      }

      // Should be able to issue a callback without the first one raising
      // an exception.
      try {
        deferredLocal.callback(null);
      } catch (Exception err) {
        deferredDone.errback(new Exception("Exception on first callback after timeout."));
        return null;
      }
      try {
        deferredLocal.callback(null);
        deferredDone.errback(new Exception("No exception on second callback after timeout."));
        return null;
      } catch (Exception err) {
        if (!(err instanceof DeferredTriggeredException)) {
          deferredDone.errback(new Exception("Incorrect exception type."));
          return null;
        }
      }
      deferredDone.callback(null);
      return null;
    }
  }

  /**
   * Test deferred timeouts (overwriting existing timeout with immediate timeout).
   * Legacy test harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ImmediateDeferredTimeout implements Deferrable, Timeable {

    private Reactor reactor;
    private Deferred deferredDone;
    private Deferred deferredLocal;
    private long timeStamp;

    /*
     * Timeable tick function is only called on reactor startup.
     */
    public void onTick(Object data) {
      System.out.println("Deferred : Testing basic deferred timeouts.");
      Object[] params = (Object[]) data;
      reactor = (Reactor) params[0];
      deferredDone = (Deferred) params[1];
      timeStamp = System.currentTimeMillis();
      deferredLocal = reactor.newDeferred();
      deferredLocal.addDeferrable(this, true);
      try {
        deferredLocal.setTimeout(1000);
        deferredLocal.setTimeout(0);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }

    /*
     * Deferrable callback should never occur.
     */
    public Object onCallback(Deferred deferred, Object data) {
      deferredDone.errback(new Exception("Unexpected callback."));
      return null;
    }

    /*
     * Deferrable errback should occur with timeout exception.
     */
    public Object onErrback(Deferred deferred, Exception error) {
      System.out.println("  Checking errback conditions.");

      // Check correct callback.
      long timeError = System.currentTimeMillis() - timeStamp;
      if ((timeError > MAX_TIMING_ERROR) && (timeError < -MAX_TIMING_ERROR)) {
        deferredDone.errback(new Exception("Deferred timeout timing error."));
        return null;
      }
      if (!(error instanceof DeferredTimedOutException)) {
        deferredDone.errback(new Exception("Unexpected exception type."));
        return null;
      }

      // Should be able to issue a callback without the first one raising
      // an exception.
      try {
        deferredLocal.callback(null);
      } catch (Exception err) {
        deferredDone.errback(new Exception("Exception on first callback after timeout."));
        return null;
      }
      try {
        deferredLocal.callback(null);
        deferredDone.errback(new Exception("No exception on second callback after timeout."));
        return null;
      } catch (Exception err) {
        if (!(err instanceof DeferredTriggeredException)) {
          deferredDone.errback(new Exception("Incorrect exception type."));
          return null;
        }
      }
      deferredDone.callback(null);
      return null;
    }
  }

  /**
   * Test deferred timeouts (with cancellation). Legacy test harness does not
   * include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class CancelledDeferredTimeout implements Deferrable, Timeable {

    private Reactor reactor;
    private Deferred deferredDone;
    private int i = 0;
    private long timeStamp;

    /*
     * Timeable tick function is only called on reactor startup.
     */
    public void onTick(Object data) {

      // First tick - set up cancelled deferred timeout and reschedule
      // timed callback to here.
      if (i == 0) {
        System.out.println("Deferred : Testing cancelled deferred timeouts.");
        Object[] params = (Object[]) data;
        reactor = (Reactor) params[0];
        deferredDone = (Deferred) params[1];
        timeStamp = System.currentTimeMillis();
        Deferred deferred = reactor.newDeferred();
        deferred.addDeferrable(this, true);
        try {
          deferred.setTimeout(1000);
          deferred.cancelTimeout();
          reactor.runTimerOneShot(this, 2000, deferred);
        } catch (Exception error) {
          deferredDone.errback(error);
        }
        i++;
      }
      // Fire deferred callback.
      else {
        System.out.println("  Firing deferred callback.");
        Deferred deferred = (Deferred) data;
        deferred.callback(null);
      }

    }

    /*
     * Deferrable callback should occur.
     */
    public Object onCallback(Deferred deferred, Object data) {
      System.out.println("  Checking callback conditions.");
      long timeError = System.currentTimeMillis() - timeStamp - 2000;
      if ((timeError > MAX_TIMING_ERROR) || (timeError < -MAX_TIMING_ERROR)) {
        deferredDone.errback(new Exception("Deferred timeout timing error."));
      } else {
        deferredDone.callback(null);
      }
      return null;
    }

    /*
     * Deferrable errback should never occur.
     */
    public Object onErrback(Deferred deferred, Exception error) {
      deferredDone.errback(new Exception("Unexpected errback."));
      return null;
    }
  }

}
