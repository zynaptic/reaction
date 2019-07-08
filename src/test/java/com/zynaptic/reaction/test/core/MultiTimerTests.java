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
import com.zynaptic.reaction.ReactorNotRunningException;

/**
 * This aggregation class contains all the tests required to exercise the
 * reactor using multiple timers.
 * 
 * @author Chris Holgate
 */
public class MultiTimerTests {

  // Define acceptable error when doing timing checks.
  private static final int MAX_TIMING_ERROR = 250;

  /*
   * Startup timeable used to issue the multiple timers.
   */
  private static class OneShotTimerTest {

    // Local handle on the reactor.
    private Reactor reactor;

    // Local handle on the test completion deferred.
    private Deferred<Object> deferredDone;

    // Count of currently active timers.
    private int timersActive;

    // Return error condition.
    private Exception returnError = null;

    /*
     * The constructor is passed a handle on the reactor, the completion deferred
     * and an array of timeouts which are to be used for the test.
     */
    public OneShotTimerTest(Reactor reactor, Deferred<Object> deferred, int[] timeoutArray) {
      this.reactor = reactor;
      this.deferredDone = deferred;
      try {
        for (int i = 0; i < timeoutArray.length; i++) {
          new OneShotTimer(i, timeoutArray[i]);
        }
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }

    /*
     * One-shot timeable object used for multi-timer tests.
     */
    private class OneShotTimer implements Timeable<String> {

      String magicData;
      long expectedTime;

      /*
       * Constructor is passed required timeout and timer ID.
       */
      public OneShotTimer(int id, int msTimeout) throws ReactorNotRunningException {
        magicData = "Test Data " + id;
        expectedTime = System.currentTimeMillis() + msTimeout;
        reactor.runTimerOneShot(this, msTimeout, magicData);
        timersActive++;
      }

      /*
       * Timer callback checks correct timing and stops reactor if this is the last
       * timer in the test.
       */
      public void onTick(String data) {

        // Verify timer callback conditions.
        long timeError = System.currentTimeMillis() - expectedTime;
        if ((timeError > MAX_TIMING_ERROR) || (timeError < -MAX_TIMING_ERROR)) {
          returnError = new Exception("Measured timer period does not match expected.");
        }
        if (data != magicData) {
          returnError = new Exception("Timer data does not match expected.");
        }

        // Signal end of test.
        if (--timersActive == 0) {
          if (returnError != null) {
            deferredDone.errback(returnError);
          } else {
            deferredDone.callback(null);
          }
        }
      }
    }
  }

  /**
   * Test one-shot timers with a range of timeout values. Legacy test harness does
   * not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class RangedOneShotTimeouts implements Timeable {
    public void onTick(Object data) {
      System.out.println("Reactor : Testing one-shot timers with multiple timeouts.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferred = (Deferred) params[1];
      int[] timeoutArray = { 5000, 0, 250, 500, 750, 1000, 1250, 1500, 1750, 2000, 2500, 3000, 3500, 4000 };
      new OneShotTimerTest(reactor, deferred, timeoutArray);
    }
  }

  /**
   * Test one-shot timers with common timeout values. Legacy test harness does not
   * include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class CommonOneShotTimeouts implements Timeable {
    public void onTick(Object data) {
      System.out.println("Reactor : Testing one-shot timers with common timeouts.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferred = (Deferred) params[1];
      int[] timeoutArray = { 5000, 0, 0, 0, 0, 1000, 1000, 1000, 1000, 2000, 2000, 2000, 2000, 3000, 3000, 3000, 3000 };
      new OneShotTimerTest(reactor, deferred, timeoutArray);
    }
  }

  /*
   * Repeating timeable object used for multi-timer tests.
   */
  private static class RepeatingTimerTest {

    // Local handle on the reactor.
    private Reactor reactor;

    // Local handle on the test completion deferred.
    private Deferred<Object> deferredDone;

    // Count of currently active timers.
    private int timersActive;

    // Return error condition.
    private Exception returnError = null;

    /*
     * The constructor is passed a handle on the reactor, the completion deferred
     * and an array of timeouts which are to be used for the test.
     */
    public RepeatingTimerTest(Reactor reactor, Deferred<Object> deferred, int[][] timeoutArray) {
      this.reactor = reactor;
      this.deferredDone = deferred;
      try {
        for (int i = 0; i < timeoutArray.length; i++) {
          int[] params = (int[]) timeoutArray[i];
          new RepeatingTimer(i, params[0], params[1], params[2]);
        }
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }

    private class RepeatingTimer implements Timeable<String> {

      String magicData;
      int interval;
      int repeatCounter;
      long expectedTime;

      /*
       * Constructor is passed required timeout and timer ID.
       */
      public RepeatingTimer(int id, int msTimeout, int msInterval, int ticks) throws ReactorNotRunningException {
        magicData = "Test Data " + id;
        interval = msInterval;
        repeatCounter = ticks;
        expectedTime = System.currentTimeMillis() + msTimeout;
        reactor.runTimerRepeating(this, msTimeout, msInterval, magicData);
        timersActive++;
      }

      /*
       * Timer callback checks correct timing and stops reactor if this is the last
       * timer in the test.
       */
      public void onTick(String data) {
        long timeError = System.currentTimeMillis() - expectedTime;
        if ((timeError > MAX_TIMING_ERROR) && (timeError < -MAX_TIMING_ERROR)) {
          returnError = new Exception("Measured timer period does not match expected.");
        }
        if (data != magicData) {
          returnError = new Exception("Timer data does not match expected.");
        }
        expectedTime += interval;
        if (--repeatCounter == 0) {
          reactor.cancelTimer(this);

          // Terminate test.
          if (--timersActive == 0) {
            if (returnError != null) {
              deferredDone.errback(returnError);
            } else {
              deferredDone.callback(null);
            }
          }
        }
      }
    }
  }

  /**
   * Test repeating timers with a range of timeout values. Legacy test harness
   * does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class RangedRepeatingTimeouts implements Timeable {
    public void onTick(Object data) {
      System.out.println("Reactor : Testing repeating timers with multiple timeouts.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferred = (Deferred) params[1];
      int[][] timeoutArray = { { 0, 250, 16 }, { 250, 500, 8 }, { 500, 750, 6 }, { 250, 1000, 4 }, { 0, 2000, 2 } };
      new RepeatingTimerTest(reactor, deferred, timeoutArray);
    }
  }

  /**
   * Test repeating timers with common timeout values. Legacy test harness does
   * not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class CommonRepeatingTimeouts implements Timeable {
    public void onTick(Object data) {
      System.out.println("Reactor : Testing repeating timers with common timeouts.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferred = (Deferred) params[1];
      int[][] timeoutArray = { { 0, 250, 8 }, { 0, 250, 8 }, { 0, 250, 8 }, { 0, 250, 8 }, { 0, 250, 8 }, { 0, 250, 8 },
          { 100, 2000, 3 }, { 100, 2000, 3 }, { 100, 2000, 3 }, { 100, 2000, 3 } };
      new RepeatingTimerTest(reactor, deferred, timeoutArray);
    }
  }
}
