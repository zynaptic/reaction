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

import com.zynaptic.reaction.DeferredTerminationException;
import com.zynaptic.reaction.DeferredTriggeredException;
import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.Reactor;
import com.zynaptic.reaction.Timeable;

/**
 * This aggregation class includes all the tests required for verifying basic
 * deferred functionality.
 * 
 * @author Chris Holgate
 */
public class DeferredBasicTests {

  /**
   * Test chained deferrable callbacks with late trigger. Legacy test harness does
   * not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static class ChainedCallbackTest implements Timeable {

    private Reactor reactor;
    private Deferred deferredDone;
    private String[] magicData;
    private Exception[] magicException;
    private DeferrableCallbackValidator[] deferrableCallbackValidators;
    private boolean[] errbackOut;
    private int triggerOffset;
    private boolean errorTrigger;
    private Deferred deferred;
    private Exception returnError = null;
    private int i = 0;

    /*
     * Set up chained callback test. The constructor is passed a handle on the
     * reactor, the 'done' deferred, the offset into the chain at which the deferred
     * is to be triggered and a list of booleans which indicates whether data or an
     * error is to be returned by a given deferrable in the chain.
     */
    private ChainedCallbackTest(Reactor reactor, Deferred deferredDone, int triggerOffset, boolean errorTrigger,
        boolean[] errbackOut) {
      this.reactor = reactor;
      this.deferredDone = deferredDone;
      this.errbackOut = errbackOut;
      this.triggerOffset = triggerOffset;
      this.errorTrigger = errorTrigger;
      magicData = new String[errbackOut.length + 1];
      magicException = new Exception[errbackOut.length + 1];
      deferrableCallbackValidators = new DeferrableCallbackValidator[errbackOut.length];
      for (int j = 0; j <= errbackOut.length; j++) {
        magicData[j] = new String("Magic Data " + j);
        magicException[j] = new Exception("Magic Error " + j);
      }
    }

    /*
     * Add a deferrable to a deferred chain.
     */
    private void addDeferrable(int index, boolean errorIn, boolean errorOut, boolean terminal) {
      System.out.println("  Add deferred : index = " + index + ", errorIn = " + errorIn + ", errorOut = " + errorOut
          + ", terminal = " + terminal + ".");
      deferrableCallbackValidators[index] = new DeferrableCallbackValidator(errorIn ? magicException[index] : null,
          magicData[index], errorOut ? magicException[index + 1] : null, magicData[index + 1]);
      deferred.addDeferrable(deferrableCallbackValidators[index], terminal);
    }

    /*
     * On first tick, set up callback chain. On second tick, verify callbacks.
     */
    public void onTick(Object data) {
      if (i == 0) {
        boolean triggered = false;
        deferred = reactor.newDeferred();
        try {
          for (int j = 0; j < errbackOut.length; j++) {

            // Trigger callbacks or errbacks at the specified point.
            if (j == triggerOffset) {
              if (errorTrigger) {
                System.out.println("  Trigger errback.");
                deferred.errback(magicException[0]);
              } else {
                System.out.println("  Trigger callback.");
                deferred.callback(magicData[0]);
              }
              triggered = true;
            }

            // First deferrable depends on trigger type.
            if (j == 0) {
              addDeferrable(j, errorTrigger, errbackOut[j], j == errbackOut.length - 1);
            } else {
              addDeferrable(j, errbackOut[j - 1], errbackOut[j], j == errbackOut.length - 1);
            }
          }

          // Add late trigger if required.
          if (!triggered) {
            if (errorTrigger) {
              System.out.println("  Trigger errback.");
              deferred.errback(magicException[0]);
            } else {
              System.out.println("  Trigger callback.");
              deferred.callback(magicData[0]);
            }
          }

          // Run timer later to check results.
          reactor.runTimerOneShot(this, 250, null);
        }

        // Trap errors adding deferrables or triggering callbacks.
        catch (Exception error) {
          deferredDone.errback(error);
        }
      }

      // On second tick, check that the deferred ran OK.
      else {
        for (int j = 0; j < errbackOut.length; j++) {
          if (deferrableCallbackValidators[j].returnError != null) {
            returnError = deferrableCallbackValidators[j].returnError;
          } else if (!deferrableCallbackValidators[j].called) {
            System.out.println("  Deferrable " + j + " not called.");
            returnError = new Exception("Deferrable " + j + " not called.");
          }
        }
        if (returnError != null) {
          deferredDone.errback(returnError);
        } else {
          deferredDone.callback(null);
        }
      }
      i++;
    }
  }

  /**
   * Test chained deferrable callbacks with early trigger. Legacy test harness
   * does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ChainedCallbacksEarlyTrigger implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred : Testing chained callbacks with early trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      boolean[] errbackOut = { false, false, false, false };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 0, false, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained deferrable callbacks with late trigger. Legacy test harness does
   * not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ChainedCallbacksLateTrigger implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred : Testing chained callbacks with late trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      boolean[] errbackOut = { false, false, false, false };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 4, false, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained deferrable callbacks with interim trigger. Legacy test harness
   * does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ChainedCallbacksInterimTrigger implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred : Testing chained callbacks with interim trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      boolean[] errbackOut = { false, false, false, false };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 2, false, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained deferrable errbacks with early trigger. Legacy test harness does
   * not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ChainedErrbacksEarlyTrigger implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred : Testing chained errbacks with early trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      boolean[] errbackOut = { true, true, true, true };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 0, true, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained deferrable errbacks with late trigger. Legacy test harness does
   * not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ChainedErrbacksLateTrigger implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred : Testing chained errbacks with late trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      boolean[] errbackOut = { true, true, true, true };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 4, true, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained deferrable errbacks with interim trigger. Legacy test harness
   * does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ChainedErrbacksInterimTrigger implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred : Testing chained errbacks with interim trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      boolean[] errbackOut = { true, true, true, true };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 2, true, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained interleaved callbacks and errbacks with early trigger. Legacy
   * test harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ChainedInterleavedEarlyTrigger implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred : Testing interleaved callbacks and errbacks with early trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      boolean[] errbackOut = { false, true, false, true, false };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 0, true, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained interleaved callbacks and errbacks with late trigger. Legacy
   * test harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ChainedInterleavedLateTrigger implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred : Testing interleaved callbacks and errbacks with late trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      boolean[] errbackOut = { false, true, false, true, false };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 5, true, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained interleaved callbacks and errbacks with interim trigger. Legacy
   * test harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ChainedInterleavedInterimTrigger implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred : Testing interleaved callbacks and errbacks with interim trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      boolean[] errbackOut = { false, true, false, true, false };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 2, true, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test to check exception generation on multiple callbacks. Legacy test harness
   * does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class MultipleCallbackTest implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred : Test multiple callback exception.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      String magicData1 = "Test data 1";
      String magicData2 = "Test data 2";
      Deferred deferred = reactor.newDeferred();
      try {
        deferred.callback(magicData1);
        deferred.callback(magicData2);
        deferredDone.errback(new Exception("Exception not thrown."));
      } catch (Exception error) {
        if (!(error instanceof DeferredTriggeredException)) {
          deferredDone.errback(new Exception("Invalid exception thrown."));
        }
      }
      deferred.addDeferrable(new DeferrableCallbackValidator(null, magicData1, null, null), true);
      deferredDone.callback(null);
    }
  }

  /**
   * Test to check exception generation on multiple errbacks. Legacy test harness
   * does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class MultipleErrbackTest implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred : Test multiple errback exception.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      Exception magicException1 = new Exception("Test exception 1");
      Exception magicException2 = new Exception("Test exception 2");
      Deferred deferred = reactor.newDeferred();
      try {
        deferred.errback(magicException1);
        deferred.errback(magicException2);
        deferredDone.errback(new Exception("Exception not thrown."));
      } catch (Exception error) {
        if (!(error instanceof DeferredTriggeredException)) {
          deferredDone.errback(new Exception("Invalid exception thrown."));
        }
      }
      deferred.addDeferrable(new DeferrableCallbackValidator(magicException1, null, null, null), true);
      deferredDone.callback(null);
    }
  }

  /**
   * Test to check exception generation on multiple terminal deferrables. Legacy
   * test harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class MultipleTerminationTest implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred : Test multiple termination exception.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      String magicData1 = "Test data 1";
      String magicData2 = "Test data 2";
      String magicData3 = "Test data 3";
      Deferred deferred = reactor.newDeferred();
      DeferrableCallbackValidator deferrable1 = new DeferrableCallbackValidator(null, magicData1, null, magicData2);
      DeferrableCallbackValidator deferrable2 = new DeferrableCallbackValidator(null, magicData2, null, magicData3);
      try {
        deferred.addDeferrable(deferrable1, true);
        deferred.addDeferrable(deferrable2, true);
        deferredDone.errback(new Exception("Exception not thrown."));
      } catch (Exception error) {
        if (!(error instanceof DeferredTerminationException)) {
          deferredDone.errback(new Exception("Invalid exception thrown."));
        }
      }
      deferred.callback(magicData1);
      deferredDone.callback(null);
    }
  }

}
