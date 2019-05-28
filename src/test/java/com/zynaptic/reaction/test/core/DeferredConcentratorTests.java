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

import java.util.Random;
import java.util.ArrayList;

import com.zynaptic.reaction.Deferrable;
import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.DeferredConcentrator;
import com.zynaptic.reaction.Reactor;
import com.zynaptic.reaction.Timeable;
import com.zynaptic.reaction.DeferredTimedOutException;

/**
 * This aggregation class contains all the testcases required to check the
 * correct functionality of the deferred callback concentrator class.
 * 
 * @author Chris Holgate
 */
public class DeferredConcentratorTests {

  /**
   * Test correct operation of deferred callback concentrator under various
   * conditions. Legacy test harness does not include strong generic type
   * checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static class DeferredConcentratorTestFixture implements Timeable {

    private static final int TEST_RUN_TIME = 4000;

    private Reactor reactor;
    private Deferred deferredDone;
    private int[] errorSet;
    private int inputCount;
    private int outputRegistrationOffset;
    private DeferredConcentrator deferredConcentrator;
    private Random random = new Random();

    /*
     * Set up the deferred callback concentrator test. The constructor is passed a
     * handle on the reactor and the deferred to be called on test completion. The
     * number of inputs is specified alongside the position in the input set at
     * which an error should be generated (negative values imply no error). Finally,
     * the offset within the input trigger sequence before the output callback chain
     * is registered is specified.
     */
    public DeferredConcentratorTestFixture(Reactor reactor, Deferred deferredDone, int inputCount, int[] errorSet,
        int outputRegistrationOffset) {
      this.reactor = reactor;
      this.deferredDone = deferredDone;
      this.errorSet = errorSet;
      this.inputCount = inputCount;
      this.outputRegistrationOffset = outputRegistrationOffset;
      deferredConcentrator = reactor.newDeferredConcentrator();
    }

    /*
     * Initiate the test on the startup timer callback.
     */
    public void onTick(Object data) {

      // Generate timed input trigger callbacks.
      int errorIndex = 0;
      for (int i = 0; i < inputCount; i++) {
        int delay = random.nextInt(TEST_RUN_TIME / 3);
        delay = (i >= outputRegistrationOffset) ? TEST_RUN_TIME - delay : delay;
        Deferred deferredCallback = reactor.newDeferred();

        // Construct anonymous class for triggering callbacks.
        Timeable timedCallback = new Timeable() {
          public void onTick(Object data) {
            Object[] params = (Object[]) data;
            Deferred deferred = (Deferred) params[0];
            try {
              Integer result = (Integer) params[1];
              System.out.println("Input callback with data = " + result.intValue());
              deferred.callback(result);
            } catch (Exception exc) {
              System.out.println("Input errback with exception = " + exc.toString());
              deferred.errback(exc);
            }
          }
        };

        // Run timed callbacks or errbacks.
        if ((errorIndex < errorSet.length) && (errorSet[errorIndex] == i)) {
          errorIndex++;
          reactor.runTimerOneShot(timedCallback, delay, new Object[] { deferredCallback, null });
        } else {
          reactor.runTimerOneShot(timedCallback, delay, new Object[] { deferredCallback, Integer.valueOf(i) });
        }

        // Add deferred to the deferred callback concentrator.
        deferredConcentrator.addInputDeferred(deferredCallback);
      }

      // Register the concentrator output handler after the specified
      // delay.
      reactor.runTimerOneShot(new ConcentratorOutputHandler(), TEST_RUN_TIME / 2, null);
    }

    /*
     * Class used for checking the output of the concentrator.
     */
    private class ConcentratorOutputHandler implements Timeable, Deferrable {

      /*
       * On timeout, the output handler requests the deferred callback output.
       */
      public void onTick(Object data) {
        Deferred outputDeferred = deferredConcentrator.getOutputDeferred();
        outputDeferred.addDeferrable(this, true);
        outputDeferred.setTimeout(TEST_RUN_TIME * 2);
        System.out.println("Added concentrator output callback chain.");
      }

      /*
       * On callback check that the length of the returned array is as expected and
       * that the array entries are in the correct order.
       */
      public Object onCallback(Deferred deferred, Object data) {
        if (errorSet.length > 0) {
          deferredDone.errback(new Exception("Expected concentrator errback but received callback instead."));
        } else {
          ArrayList results = (ArrayList) data;
          boolean orderOk = true;
          if (results.size() != inputCount) {
            deferredDone.errback(new Exception("Incorrect number of results in deferred concentrator output."));
          } else {
            for (int i = 0; i < inputCount; i++) {
              if (i != ((Integer) results.get(i)).intValue())
                orderOk = false;
            }
            if (!orderOk) {
              deferredDone.errback(new Exception("Incorrect result ordering in deferred concentrator output."));
            } else {
              deferredDone.callback(null);
            }
          }
        }
        return null;
      }

      /*
       * On errback check that the returned exception is as expected. Wait for
       * completion until the test is complete to ensure no false callback triggers
       * are generated.
       */
      public Object onErrback(Deferred deferred, Exception error) {
        if (error instanceof DeferredTimedOutException) {
          deferredDone.errback(new Exception("Deferred callback concentrator timed out during test."));
        } else if (errorSet.length == 0) {
          deferredDone.errback(new Exception("Expected concentrator callback but received errback instead."));
        } else {
          Timeable timedCallback = new Timeable() {
            public void onTick(Object data) {
              deferredDone.callback(null);
            }
          };
          reactor.runTimerOneShot(timedCallback, TEST_RUN_TIME, null);
        }
        return null;
      }
    }
  }

  /**
   * Test deferred callback concentrator with early trigger. Legacy test harness
   * does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredConcentratorEarlyCallback implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Concentrator : Testing early callback.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredConcentratorTestFixture callbackTest = new DeferredConcentratorTestFixture(reactor, deferredDone, 10,
          new int[] {}, 10);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test deferred callback concentrator with late trigger. Legacy test harness
   * does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredConcentratorLateCallback implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Concentrator : Testing late callback.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredConcentratorTestFixture callbackTest = new DeferredConcentratorTestFixture(reactor, deferredDone, 10,
          new int[] {}, 0);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test deferred callback concentrator with interim trigger. Legacy test harness
   * does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredConcentratorInterimCallback implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Concentrator : Testing interim callback.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredConcentratorTestFixture callbackTest = new DeferredConcentratorTestFixture(reactor, deferredDone, 10,
          new int[] {}, 5);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test deferred callback concentrator with early error callback. Legacy test
   * harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredConcentratorEarlyErrback implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Concentrator : Testing early errback.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredConcentratorTestFixture callbackTest = new DeferredConcentratorTestFixture(reactor, deferredDone, 10,
          new int[] { 2 }, 5);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test deferred callback concentrator with late error callback. Legacy test
   * harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredConcentratorLateErrback implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Concentrator : Testing late errback.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredConcentratorTestFixture callbackTest = new DeferredConcentratorTestFixture(reactor, deferredDone, 10,
          new int[] { 7 }, 5);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test deferred callback concentrator with early multiple error callbacks.
   * Legacy test harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredConcentratorEarlyMultipleErrbacks implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Concentrator : Testing early multiple errbacks.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredConcentratorTestFixture callbackTest = new DeferredConcentratorTestFixture(reactor, deferredDone, 10,
          new int[] { 1, 2, 3 }, 5);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test deferred callback concentrator with late multiple error callbacks.
   * Legacy test harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredConcentratorLateMultipleErrbacks implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Concentrator : Testing late multiple errbacks.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredConcentratorTestFixture callbackTest = new DeferredConcentratorTestFixture(reactor, deferredDone, 10,
          new int[] { 6, 7, 8 }, 5);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test deferred callback concentrator with spanning multiple error callbacks.
   * Legacy test harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredConcentratorSpanningMultipleErrbacks implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Concentrator : Testing spanning multiple errbacks.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredConcentratorTestFixture callbackTest = new DeferredConcentratorTestFixture(reactor, deferredDone, 10,
          new int[] { 2, 3, 7, 8 }, 5);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

}
