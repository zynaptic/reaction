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
import com.zynaptic.reaction.Signal;
import com.zynaptic.reaction.Signalable;
import com.zynaptic.reaction.Timeable;
import com.zynaptic.reaction.SignalContextException;

/**
 * This aggregation class encapsulates all the tests which are required to
 * verify correct functioning of the reactor signal code.
 * 
 * @author Chris Holgate
 */
public class SignalTests {

  // Interval between test 'tick' callbacks in milliseconds.
  private static final int TICK_INTERVAL = 250;

  /**
   * Test single signal subscription, generation and unsubscription. Legacy test
   * harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class SingleSignalCallback implements Timeable, Signalable {

    private Reactor reactor;
    private Deferred deferredDone;
    private int i = 0;
    private int callbackCount = 0;
    private String magicData1 = "Test data 1";
    private Signal testSignal;

    /*
     * Timer tick method - used to start up test and generate signal events.
     */
    public void onTick(Object data) {

      // On first call, register for signal callbacks and then generate
      // signal.
      if (i == 0) {
        System.out.println("Reactor : Testing single signal callbacks.");
        Object[] params = (Object[]) data;
        reactor = (Reactor) params[0];
        deferredDone = (Deferred) params[1];
        testSignal = reactor.newSignal();
        try {
          reactor.runTimerOneShot(this, TICK_INTERVAL, null);
          testSignal.subscribe(this);
          testSignal.signal(magicData1);
        } catch (Exception error) {
          deferredDone.errback(error);
        }
      }

      // On second callback, check that the signal has fired once then try
      // to re-subscribe to the same signal.
      else if (i == 1) {
        if (callbackCount != 1) {
          deferredDone.errback(new Exception("Incorrect number of signal callbacks detected."));
        } else {
          try {
            reactor.runTimerOneShot(this, TICK_INTERVAL, null);
            testSignal.subscribe(this);
            testSignal.signal(magicData1);
          } catch (Exception error) {
            deferredDone.errback(error);
          }
        }
      }

      // On third callback, check that the signal has fired once then
      // unsubscribe from the signal.
      else if (i == 2) {
        if (callbackCount != 2) {
          deferredDone.errback(new Exception("Incorrect number of signal callbacks detected."));
        } else {
          try {
            reactor.runTimerOneShot(this, TICK_INTERVAL, null);
            testSignal.unsubscribe(this);
            testSignal.signal(magicData1);
          } catch (Exception error) {
            deferredDone.errback(error);
          }
        }
      }

      // On fourth callback, check that the signal did not fire and
      // attempt to
      // unsubscribe again.
      else if (i == 3) {
        if (callbackCount != 2) {
          deferredDone.errback(new Exception("Incorrect number of signal callbacks detected."));
        } else {
          try {
            reactor.runTimerOneShot(this, TICK_INTERVAL, null);
            testSignal.unsubscribe(this);
            testSignal.signal(magicData1);
          } catch (Exception error) {
            deferredDone.errback(error);
          }
        }
      }

      // On final callback, check that the signal did not fire and exit to
      // the
      // test fixture.
      else {
        if (callbackCount != 2) {
          deferredDone.errback(new Exception("Incorrect number of signal callbacks detected."));
        } else {
          deferredDone.callback(null);
        }
      }
      i++;
    }

    /*
     * Signal callback method. This checks for valid callback data and ups the
     * callback counter.
     */
    public void onSignal(Signal signalId, Object data) {
      if (!(signalId.equals(testSignal))) {
        deferredDone.errback(new Exception("Signal callback with incorrect signal ID."));
      } else if (data != magicData1) {
        deferredDone.errback(new Exception("Signal callback with invalid data."));
      } else {
        callbackCount++;
        System.out.println("  Valid signal callback : " + callbackCount);
      }
    }
  }

  /**
   * Test exception generation for invalid operations. Legacy test harness does
   * not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class InvalidOperationExceptions implements Timeable, Signalable {

    private Reactor reactor;
    private Deferred deferredDone;
    private int i = 0;
    private int callbackCount = 0;
    private String magicData1 = "Test data 1";
    private String magicData2 = "Test data 2";
    private Signal testSignal1;
    private Signal testSignal2;

    /*
     * Timer tick method - used to start up test and generate signal events.
     */
    public void onTick(Object data) {

      // On first call, register for signal callbacks and then generate
      // signal.
      if (i == 0) {
        System.out.println("Reactor : Testing signal exceptions for invalid operations.");
        Object[] params = (Object[]) data;
        reactor = (Reactor) params[0];
        deferredDone = (Deferred) params[1];
        testSignal1 = reactor.newSignal();
        testSignal2 = reactor.newSignal();

        // Add a signal callback so that we can test signal context
        // exceptions.
        try {
          reactor.runTimerOneShot(this, TICK_INTERVAL, null);
          testSignal1.subscribe(this);
          testSignal2.subscribe(this);
          testSignal1.signal(magicData1);
        } catch (Exception error) {
          deferredDone.errback(error);
        }

      }

      // On second callback test has completed. Notify test harness.
      else {
        if (callbackCount != 2) {
          deferredDone.errback(new Exception("Incorrect number of signal callbacks detected."));
        } else {
          deferredDone.callback(null);
        }
      }
      i++;
    }

    /*
     * Signal callback method. This attempts to subscribe and unsubscribe the
     * signalable and fire a new signal from the signal callback context.
     */
    public void onSignal(Signal signalId, Object data) {

      // First signal callback should be for signal 1.
      if (i == 1) {
        if (!(signalId.equals(testSignal1))) {
          deferredDone.errback(new Exception("Signal callback with incorrect signal ID."));
        } else if (data != magicData1) {
          deferredDone.errback(new Exception("Signal callback with invalid data."));
        } else {
          callbackCount++;
          System.out.println("  Valid signal callback : " + callbackCount);

          // We should not be able to call the subscribeSignalable
          // method in
          // this
          // context.
          try {
            testSignal1.subscribe(this);
            deferredDone.errback(new Exception("Expected SignalContextException in signal callback."));
          } catch (SignalContextException error) {
            // This is expected.
          } catch (Exception error) {
            deferredDone.errback(new Exception("Unexpected exception in signal callback."));
          }

          // We should not be able to call the unsubscribeSignalable
          // method in
          // this context.
          try {
            testSignal1.unsubscribe(this);
            deferredDone.errback(new Exception("Expected SignalContextException in signal callback."));
          } catch (SignalContextException error) {
            // This is expected.
          } catch (Exception error) {
            deferredDone.errback(new Exception("Unexpected exception in signal callback."));
          }

          // We should be able to fire a new signal in this context.
          try {
            testSignal2.signal(magicData2);
          } catch (Exception error) {
            deferredDone.errback(new Exception("Unexpected exception in signal callback."));
          }
        }
      }
      // Second signal callback should be for signal 2.
      else {
        if (!(signalId.equals(testSignal2))) {
          deferredDone.errback(new Exception("Signal callback with incorrect signal ID."));
        } else if (data != magicData2) {
          deferredDone.errback(new Exception("Signal callback with invalid data."));
        } else {
          callbackCount++;
          System.out.println("  Valid signal callback : " + callbackCount);
        }
      }
      i++;
    }
  }

  /**
   * Test final signal operation. Legacy test harness does not include strong
   * generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class FinalSignalCallback implements Timeable, Signalable {

    private Reactor reactor;
    private Deferred deferredDone;
    private int i = 0;
    private int callbackCount = 0;
    private String magicData1 = "Test data 1";
    private Signal testSignal;

    /*
     * Timer tick method - used to start up test and generate signal events.
     */
    public void onTick(Object data) {

      // On first call, register for signal callbacks and then generate
      // signal.
      if (i == 0) {
        System.out.println("Reactor : Testing final signal callbacks.");
        Object[] params = (Object[]) data;
        reactor = (Reactor) params[0];
        deferredDone = (Deferred) params[1];
        testSignal = reactor.newSignal();
        try {
          reactor.runTimerOneShot(this, TICK_INTERVAL, null);
          testSignal.subscribe(this);
          testSignal.signal(magicData1);
        } catch (Exception error) {
          deferredDone.errback(error);
        }
      }

      // On second callback, check that the signal has fired once then
      // perform a
      // final signal callback.
      else if (i == 1) {
        if (callbackCount != 1) {
          deferredDone.errback(new Exception("Incorrect number of signal callbacks detected."));
        } else {
          try {
            reactor.runTimerOneShot(this, TICK_INTERVAL, null);
            testSignal.signalFinalize(magicData1);
          } catch (Exception error) {
            deferredDone.errback(error);
          }
        }
      }

      // On third callback, check that the signal has fired once then
      // attempt to
      // signal again.
      else if (i == 2) {
        if (callbackCount != 2) {
          deferredDone.errback(new Exception("Incorrect number of signal callbacks detected."));
        } else {
          try {
            reactor.runTimerOneShot(this, TICK_INTERVAL, null);
            testSignal.signal(magicData1);
          } catch (Exception error) {
            deferredDone.errback(error);
          }
        }
      }

      // On final callback, check that the signal did not fire and exit to
      // the
      // test fixture.
      else {
        if (callbackCount != 2) {
          deferredDone.errback(new Exception("Incorrect number of signal callbacks detected."));
        } else {
          deferredDone.callback(null);
        }
      }
      i++;
    }

    /*
     * Signal callback method. This checks for valid callback data and ups the
     * callback counter.
     */
    public void onSignal(Signal signalId, Object data) {
      if (!(signalId.equals(testSignal))) {
        deferredDone.errback(new Exception("Signal callback with incorrect signal ID."));
      } else if (data != magicData1) {
        deferredDone.errback(new Exception("Signal callback with invalid data."));
      } else {
        callbackCount++;
        System.out.println("  Valid signal callback : " + callbackCount);
      }
    }
  }
}
