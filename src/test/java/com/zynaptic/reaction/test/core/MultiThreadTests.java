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

import com.zynaptic.reaction.Deferrable;
import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.Reactor;
import com.zynaptic.reaction.Threadable;
import com.zynaptic.reaction.Timeable;
import com.zynaptic.reaction.ReactorNotRunningException;

/**
 * This aggregation class contains all the tests required to exercise the
 * multi-thread behaviour of the reactor.
 * 
 * @author Chris Holgate
 */
public class MultiThreadTests {

  // Define acceptable error when doing timing checks.
  private static final int MAX_TIMING_ERROR = 250;

  /*
   * Test multiple threads with provided parameters.
   */
  private static class TestRepeatingThreadable {

    // Local handle on the reactor.
    private Reactor reactor;

    // Local handle on the completion deferred.
    private Deferred<Object> deferredDone;

    // Track number of currently active threads.
    private int threadsActive = 0;

    // Return error condition.
    private volatile Exception returnError;

    /*
     * The constructor is passed a handle on the reactor, the deferred to use for
     * completion callbacks and an array of threadable parameters which are to be
     * used for the test.
     */
    public TestRepeatingThreadable(Reactor reactor, Deferred<Object> deferred, Object[] timeoutArray) {
      this.reactor = reactor;
      this.deferredDone = deferred;
      try {
        for (int i = 0; i < timeoutArray.length; i++) {
          int[] params = (int[]) timeoutArray[i];
          new RepeatingThreadable(i, params[0], params[1], params[2], params[3] != 0);
        }
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }

    /*
     * Generic threadable object used in multi-thread tests. Legacy test harness
     * does not include strong generic type checking.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private class RepeatingThreadable implements Threadable, Deferrable, Timeable {

      String magicDataIn;
      String magicDataOut;
      Exception magicException;
      long timeStamp;
      int repeatCounter;
      int msDelay;
      int msInterval;
      boolean throwsException;

      /*
       * Constructor passes test setup parameters.
       */
      public RepeatingThreadable(int id, int msDelay, int msInterval, int repeats, boolean throwsException)
          throws ReactorNotRunningException {
        magicDataIn = "Test data in " + id;
        magicDataIn = "Test data out " + id;
        magicException = new Exception("Test exception " + id);
        timeStamp = System.currentTimeMillis();
        this.repeatCounter = repeats;
        this.msDelay = msDelay;
        this.msInterval = msInterval;
        this.throwsException = throwsException;
        reactor.runThread(this, magicDataIn).addDeferrable(this, true);
        threadsActive++;
      }

      /*
       * Callback on thread completion.
       */
      public Object onCallback(Deferred deferred, Object data) {

        // Fail if we're expecting an errback.
        if (throwsException) {
          returnError = new Exception("Unexpected callback.");
        }

        // Verify callback data.
        long timingError = System.currentTimeMillis() - msDelay - timeStamp;
        if ((timingError > MAX_TIMING_ERROR) || (timingError < -MAX_TIMING_ERROR)) {
          returnError = new Exception("Timing error in callback.");
        }
        if (data != magicDataOut) {
          returnError = new Exception("Mismatched data in callback.");
        }

        // Insert the inter-thread delay.
        try {
          reactor.runTimerOneShot(this, msInterval, null);
        } catch (Exception error) {
          returnError = error;
        }
        return null;
      }

      /*
       * Callback on thread error completion.
       */
      public Object onErrback(Deferred deferred, Exception error) {

        // Fail if we're expecting a callback.
        if (!throwsException) {
          returnError = new Exception("Unexpected errback : " + error.toString());
        }

        // Verify callback data.
        long timingError = System.currentTimeMillis() - msDelay - timeStamp;
        if ((timingError > MAX_TIMING_ERROR) || (timingError < -MAX_TIMING_ERROR)) {
          returnError = new Exception("Timing error in errback.");
        }
        if (error != magicException) {
          returnError = new Exception("Mismatched exception in errback.");
        }

        // Insert the inter-thread delay.
        try {
          reactor.runTimerOneShot(this, msInterval, null);
        } catch (Exception err) {
          returnError = err;
        }
        return null;
      }

      /*
       * Threadable method call.
       */
      public Object run(Object data) throws Exception {

        // Check data passed in.
        if (data != magicDataIn) {
          returnError = new Exception("Mismatched parameter passed to thread.");
        }

        // Implement delay.
        // logService.log(ReactorLogTarget.LOG_DEBUG, " Processing : " +
        // data);
        Thread.sleep(msDelay);

        // Return or throw exception as required.
        if (throwsException) {
          throw magicException;
        } else {
          return magicDataOut;
        }
      }

      /*
       * Timeable tick callback - used to reschedule the threadable.
       */
      public void onTick(Object data) {
        if (--repeatCounter != 0) {
          timeStamp = System.currentTimeMillis();
          try {
            reactor.runThread(this, magicDataIn).addDeferrable(this, true);
          } catch (Exception error) {
            deferredDone.errback(error);
          }
        } else if (--threadsActive == 0) {
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
   * Test repeating threadables with a range of timeout values and callbacks.
   * Legacy test harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class RangedThreadCallbacks implements Timeable {
    public void onTick(Object data) {
      System.out.println("Reactor : Testing multiple threads with a range of intervals.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      int[][] timeoutArray = { { 250, 100, 16, 0 }, { 550, 240, 8, 0 }, { 1050, 320, 4, 0 }, { 2050, 490, 2, 0 },
          { 2550, 512, 2, 0 }, { 4050, 675, 1, 0 } };
      new TestRepeatingThreadable(reactor, deferredDone, timeoutArray);
    }
  }

  /**
   * Test repeating threadables with common timeout values and callbacks. Legacy
   * test harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class CommonThreadCallbacks implements Timeable {
    public void onTick(Object data) {
      System.out.println("Reactor : Testing multiple threads with common intervals.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      int[][] timeoutArray = { { 1000, 250, 4, 0 }, { 1000, 250, 4, 0 }, { 1000, 250, 4, 0 }, { 1000, 250, 4, 0 },
          { 2100, 250, 2, 0 }, { 2100, 250, 2, 0 }, { 2100, 250, 2, 0 }, { 2100, 250, 2, 0 } };
      new TestRepeatingThreadable(reactor, deferredDone, timeoutArray);
    }
  }

  /**
   * Test repeating threadables with a range of timeout values and errbacks.
   * Legacy test harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class RangedThreadErrbacks implements Timeable {
    public void onTick(Object data) {
      System.out.println("Reactor : Testing multiple threads with errbacks and a range of intervals.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      int[][] timeoutArray = { { 150, 200, 16, 1 }, { 350, 280, 8, 1 }, { 105, 320, 4, 1 }, { 50, 2490, 2, 1 },
          { 2550, 512, 2, 1 }, { 4050, 675, 1, 1 } };
      new TestRepeatingThreadable(reactor, deferredDone, timeoutArray);
    }
  }
}
