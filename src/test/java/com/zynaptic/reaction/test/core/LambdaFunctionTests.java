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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.Reactor;
import com.zynaptic.reaction.Timeable;

/**
 * This aggregation class includes all the tests required for verifying the
 * enhanced lambda function API.
 * 
 * @author Chris Holgate
 */
public class LambdaFunctionTests {

  /**
   * Test chained deferrable callbacks with late trigger.
   */
  private static class ChainedCallbackTest implements Timeable<String> {

    private Reactor reactor;
    private Deferred<Object> deferredDone;
    private String[] magicData;
    private Exception[] magicException;
    private DeferrableCallbackValidator[] deferrableCallbackValidators;
    private boolean[] errbackOut;
    private int triggerOffset;
    private int callbackOffset;
    private int errbackOffset;
    private boolean errorTrigger;
    private Deferred<Object> deferred;
    private Exception returnError = null;
    private int i = 0;

    /*
     * Set up chained callback test. The constructor is passed a handle on the
     * reactor, the 'done' deferred, the offset into the chain at which the deferred
     * is to be triggered and a list of booleans which indicates whether data or an
     * error is to be returned by a given deferrable in the chain.
     */
    private ChainedCallbackTest(Reactor reactor, Deferred<Object> deferredDone, int triggerOffset, int callbackOffset,
        int errbackOffset, boolean errorTrigger, boolean[] errbackOut) {
      this.reactor = reactor;
      this.deferredDone = deferredDone;
      this.errbackOut = errbackOut;
      this.triggerOffset = triggerOffset;
      this.callbackOffset = callbackOffset;
      this.errbackOffset = errbackOffset;
      this.errorTrigger = errorTrigger;
      magicData = new String[errbackOut.length + 1];
      magicException = new Exception[errbackOut.length + 1];
      deferrableCallbackValidators = new DeferrableCallbackValidator[errbackOut.length];
      magicException[0] = new Exception("Magic Error X");
      magicData[0] = new String("Magic Data D");
      for (int j = 1; j <= errbackOut.length; j++) {
        magicException[j] = new Exception(magicException[j - 1].getMessage() + " X");
        if (j == errbackOffset) {
          magicData[j] = magicException[j].getMessage();
        } else {
          magicData[j] = magicData[j - 1] + " D";
        }
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
    public void onTick(String data) {
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
            }

            // Add callback function.
            else if (j == callbackOffset) {
              System.out.println("  Add callback function : index = " + j);
              deferred.addCallback(x -> x + " D");
            }

            // Add errback function.
            else if (j == errbackOffset) {
              System.out.println("  Add errback function : index = " + j);
              deferred.addErrback(x -> x.getMessage() + " D");
            }

            // Add standard deferrable.
            else {
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
          if ((j != callbackOffset) && (j != errbackOffset)) {
            if (deferrableCallbackValidators[j].returnError != null) {
              returnError = deferrableCallbackValidators[j].returnError;
            } else if (!deferrableCallbackValidators[j].called) {
              System.out.println("  Deferrable " + j + " not called.");
              returnError = new Exception("Deferrable " + j + " not called.");
            }
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
   * Test chained deferrable callbacks with early trigger. Uses inserted lambda
   * callback function.
   */
  public static class ChainedCallbacksEarlyTrigger implements Timeable<Object> {
    @SuppressWarnings("unchecked")
    public void onTick(Object data) {
      System.out.println("Deferred : Testing chained callbacks with early trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred<Object> deferredDone = (Deferred<Object>) params[1];
      boolean[] errbackOut = { false, false, false, false };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 0, 1, 0, false, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained deferrable callbacks with late trigger. Uses inserted lambda
   * callback function.
   */
  public static class ChainedCallbacksLateTrigger implements Timeable<Object> {
    @SuppressWarnings("unchecked")
    public void onTick(Object data) {
      System.out.println("Deferred : Testing chained callbacks with late trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred<Object> deferredDone = (Deferred<Object>) params[1];
      boolean[] errbackOut = { false, false, false, false };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, -1, 2, 0, false, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained deferrable callbacks with interim trigger. Uses inserted lambda
   * callback function.
   */
  public static class ChainedCallbacksInterimTrigger implements Timeable<Object> {
    @SuppressWarnings("unchecked")
    public void onTick(Object data) {
      System.out.println("Deferred : Testing chained callbacks with interim trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred<Object> deferredDone = (Deferred<Object>) params[1];
      boolean[] errbackOut = { false, false, false, false };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 2, 2, 0, false, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained deferrable error callbacks with early trigger. Uses inserted
   * lambda errback function.
   */
  public static class ChainedErrbacksEarlyTrigger implements Timeable<Object> {
    @SuppressWarnings("unchecked")
    public void onTick(Object data) {
      System.out.println("Deferred : Testing chained error callbacks with early trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred<Object> deferredDone = (Deferred<Object>) params[1];
      boolean[] errbackOut = { true, false, true, false };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 0, 0, 1, false, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained deferrable error callbacks with late trigger. Uses inserted
   * lambda errback function.
   */
  public static class ChainedErrbacksLateTrigger implements Timeable<Object> {
    @SuppressWarnings("unchecked")
    public void onTick(Object data) {
      System.out.println("Deferred : Testing chained error callbacks with late trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred<Object> deferredDone = (Deferred<Object>) params[1];
      boolean[] errbackOut = { true, true, false, false };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, -1, 0, 2, false, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test chained deferrable error callbacks with interim trigger. Uses inserted
   * lambda errback function.
   */
  public static class ChainedErrbacksInterimTrigger implements Timeable<Object> {
    @SuppressWarnings("unchecked")
    public void onTick(Object data) {
      System.out.println("Deferred : Testing chained error callbacks with interim trigger.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred<Object> deferredDone = (Deferred<Object>) params[1];
      boolean[] errbackOut = { true, true, false, false };
      ChainedCallbackTest callbackTest = new ChainedCallbackTest(reactor, deferredDone, 2, 0, 2, false, errbackOut);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test run later capability with a single input argument to the lambda
   * function.
   */
  public static class RunLaterSingleArg implements Timeable<Object[]> {

    private Reactor reactor;
    private Deferred<Integer> deferredDone;
    private int i = 0;
    private boolean[] flags = new boolean[] { false };

    @SuppressWarnings("unchecked")
    @Override
    public void onTick(Object[] params) {

      // On first call, initiate test.
      if (i == 0) {
        reactor = (Reactor) params[0];
        deferredDone = (Deferred<Integer>) params[1];
        i += 1;
        reactor.runTimerOneShot(this, 1000, null);

        // Run the timed lambda function.
        Consumer<boolean[]> function = (boolean[] flags) -> {
          System.out.println("Lambda timer completed");
          flags[0] = true;
        };
        reactor.runLater(function, 500, flags);
      }

      // Callback after test - notify completion.
      else {
        if (flags[0]) {
          deferredDone.callback(0);
        } else {
          deferredDone.errback(new Exception("Run later function not executed"));
        }
      }
    }
  }

  /**
   * Test run later capability with dual input arguments to the lambda function.
   */
  public static class RunLaterDualArg implements Timeable<Object[]> {

    private Reactor reactor;
    private Deferred<Integer> deferredDone;
    private int i = 0;
    private boolean[] flags = new boolean[] { false };
    private int[] values = new int[] { 0 };

    @SuppressWarnings("unchecked")
    @Override
    public void onTick(Object[] params) {

      // On first call, initiate test.
      if (i == 0) {
        reactor = (Reactor) params[0];
        deferredDone = (Deferred<Integer>) params[1];
        i += 1;
        reactor.runTimerOneShot(this, 1000, null);

        // Run the timed lambda function.
        BiConsumer<boolean[], int[]> function = (boolean[] flags, int[] values) -> {
          System.out.println("Lambda timer completed");
          flags[0] = true;
          values[0] = 42;
        };
        reactor.runLater(function, 500, flags, values);
      }

      // Callback after test - notify completion.
      else {
        if (flags[0] && (values[0] == 42)) {
          deferredDone.callback(0);
        } else {
          deferredDone.errback(new Exception("Run later function not executed"));
        }
      }
    }
  }
}
