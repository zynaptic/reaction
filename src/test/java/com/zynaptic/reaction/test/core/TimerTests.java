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

import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.Reactor;
import com.zynaptic.reaction.Timeable;

/**
 * This aggregation class holds all the tests used to verify single timer
 * behaviour.
 * 
 * @author Chris Holgate
 */
public class TimerTests {

  // Define acceptable error when doing timing checks.
  private static final int MAX_TIMING_ERROR = 250;

  /**
   * Timeout test for one-shot timeouts. Legacy test harness does not include
   * strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class OneShotTimeouts implements Timeable {

    private Reactor reactor;
    private Deferred deferredDone;
    private int i = 0;
    private long timeStamp;
    private String[] magicData = { "Test Data 1", "Test Data 2", "Test Data 3", "Test Data 4" };
    private int[] timeouts = { 500, 1000, 2000, 4000 };

    /*
     * This is the timer 'tick' callback.
     */
    public void onTick(Object data) {

      // First call passes the deferred and reactor handle as parameters.
      if (i == 0) {
        System.out.println("Reactor : Testing one-shot timer timeouts.");
        Object[] params = (Object[]) data;
        reactor = (Reactor) params[0];
        deferredDone = (Deferred) params[1];
      }

      // Check expected timeout and returned data.
      else {
        long timeError = System.currentTimeMillis() - timeStamp - timeouts[i - 1];
        if ((timeError > MAX_TIMING_ERROR) || (timeError < -MAX_TIMING_ERROR)) {
          deferredDone.errback(new Exception("Measured timer period does not match expected."));
        }
        if (data != magicData[i - 1]) {
          deferredDone.errback(new Exception("Data mismatch on timer callback."));
        }
      }

      // Run the next timer test.
      if (i < 4) {
        System.out.println("  Running timer for " + timeouts[i] + " ms.");
        try {
          reactor.runTimerOneShot(this, timeouts[i], magicData[i]);
        } catch (Exception error) {
          deferredDone.errback(error);
        }
      }

      // Test done - stop reactor.
      else {
        deferredDone.callback(null);
      }
      timeStamp = System.currentTimeMillis();
      i++;
    }
  }

  /**
   * Timer rescheduling and cancellation test for one-shot timeouts. Legacy test
   * harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class OneShotCancellation implements Timeable {

    private Reactor reactor;
    private Deferred deferredDone;
    private int i = 0;
    private Timeable dontCall = new TimeableNeverCalled();
    private boolean cancellationFailed = false;

    /*
     * Timeable callback that should never be called.
     */
    private class TimeableNeverCalled implements Timeable {
      public void onTick(Object data) {
        cancellationFailed = true;
      }
    }

    /*
     * This is the timer 'tick' callback.
     */
    public void onTick(Object data) {

      // First call passes the deferred and reactor handle as parameters.
      if (i == 0) {
        System.out.println("Reactor : Testing one-shot timer rescheduling and cancellation.");
        Object[] params = (Object[]) data;
        reactor = (Reactor) params[0];
        deferredDone = (Deferred) params[1];
      }

      // First, schedule a callback that will be rescheduled and a
      // callback here to do the rescheduling. Then reschedule the
      // callback a few times to prevent it occuring.
      if (i < 4) {
        System.out.println("  Rescheduling timer.");
        try {
          reactor.runTimerOneShot(dontCall, 1000, null);
          reactor.runTimerOneShot(this, 500, null);
        } catch (Exception error) {
          deferredDone.errback(error);
        }
      }

      // Cancel the rescheduled callback and check that it does not fire.
      else if (i == 4) {
        System.out.println("  Cancelling timer.");
        try {
          reactor.cancelTimer(dontCall);
          reactor.runTimerOneShot(this, 2000, null);
        } catch (Exception error) {
          deferredDone.errback(error);
        }
      }

      // Test complete - return result.
      else {
        if (cancellationFailed) {
          deferredDone.errback(new Exception("Timer cancellation failed."));
        } else {
          deferredDone.callback(null);
        }
      }
      i++;
    }
  }

  /**
   * Timeable callback for repeating timer interval and cancellation test. Legacy
   * test harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class RepeatingTimerInterval implements Timeable {

    private Reactor reactor;
    private Deferred deferredDone;
    private int i = 0;
    private long timeStamp;
    private String magicData = "Test Data 1";

    /*
     * Timeout on timer cancellation test. Reports successful completion.
     */
    private class TimeableRepeatingTimerStop implements Timeable {
      public void onTick(Object data) {
        deferredDone.callback(null);
      }
    }

    /*
     * This is the timer 'tick' callback.
     */
    public void onTick(Object data) {

      // First call passes the deferred and reactor handle as parameters.
      if (i == 0) {
        System.out.println("Reactor : Testing repeating timer interval and cancellation.");
        Object[] params = (Object[]) data;
        reactor = (Reactor) params[0];
        deferredDone = (Deferred) params[1];
        try {
          reactor.runTimerRepeating(this, 2000, 1000, magicData);
        } catch (Exception error) {
          deferredDone.errback(error);
        }
      }

      // On subsequent callbacks, check expected timing.
      else if (i <= 4) {
        System.out.println("  Repeating timer callback.");
        long timeError = System.currentTimeMillis() - timeStamp - ((i == 1) ? 2000 : 1000);
        if ((timeError > MAX_TIMING_ERROR) || (timeError < -MAX_TIMING_ERROR)) {
          deferredDone.errback(new Exception("Measured timer period does not match expected."));
        }
        if (data != magicData) {
          deferredDone.errback(new Exception("Data mismatch on timer callback."));
        }
      }

      // On final callback, cancel and schedule reactor stop.
      if (i == 4) {
        try {
          reactor.cancelTimer(this);
          reactor.runTimerOneShot(new TimeableRepeatingTimerStop(), 2000, null);
        } catch (Exception error) {
          deferredDone.errback(error);
        }
      }

      // Subsequent callbacks are an error condition.
      if (i > 4) {
        deferredDone.errback(new Exception("Repeating timer not cancelled."));
      }
      timeStamp = System.currentTimeMillis();
      i++;
    }
  }

}
