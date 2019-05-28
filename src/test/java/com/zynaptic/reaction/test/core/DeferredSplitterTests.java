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
import com.zynaptic.reaction.DeferredSplitter;
import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.Reactor;
import com.zynaptic.reaction.Timeable;

/**
 * This aggregation class contains all the tests required to verify the correct
 * behaviour of deferred callback splitters.
 * 
 * @author Chris Holgate
 */
public class DeferredSplitterTests {

  /**
   * Test correct operation of deferred callback splitter under various
   * conditions. Legacy test harness does not include strong generic type
   * checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static class DeferredSplitterTestFixture implements Timeable, Deferrable {

    private Reactor reactor;
    private Deferred deferredDone;
    private Deferred deferredTrigger;
    private DeferredSplitter deferredSplitter;
    private int triggerOffset;
    private boolean errorTrigger;
    private int listLength;
    private Exception magicException = null;
    private String magicString = "Input data.";
    private String outputData = "Output data.";
    private int i = 0;
    private Exception callbackError = null;
    private int callbackCount = 0;

    /*
     * Set up deferred callback splitter test. The constructor is passed a handle on
     * the reactor, the 'done' deferred, the offset into the list at which the
     * deferred is to be triggered and a flag indicating whether callbacks or
     * errbacks are to be generated.
     */
    private DeferredSplitterTestFixture(Reactor reactor, Deferred deferredDone, int triggerOffset, boolean errorTrigger,
        int listLength) {
      this.reactor = reactor;
      this.deferredDone = deferredDone;
      this.triggerOffset = triggerOffset;
      this.errorTrigger = errorTrigger;
      this.listLength = listLength;
      this.deferredSplitter = reactor.newDeferredSplitter();
      if (errorTrigger) {
        magicException = new Exception("Input exception.");
      }
      deferredTrigger = reactor.newDeferred();
      deferredSplitter.addInputDeferred(deferredTrigger);
    }

    /*
     * Callback on timer ticks. One deferred is added to the list on each tick.
     */
    public void onTick(Object data) {

      // Trigger the deferrable list at the appropriate offset.
      if (i == triggerOffset) {
        if (errorTrigger) {
          deferredTrigger.errback(magicException);
        } else {
          deferredTrigger.callback(magicString);
        }
      }

      // Add a deferrable to the list.
      if (i < listLength) {
        Deferred deferred = deferredSplitter.getOutputDeferred();
        deferred.addDeferrable(new DeferrableCallbackValidator(magicException, magicString, null, outputData), false);
        deferred.addDeferrable(this, true);
      }

      // At end of test, check the callback count.
      if (i > listLength) {
        if (callbackError != null) {
          deferredDone.errback(callbackError);
        } else if (callbackCount != listLength) {
          deferredDone.errback(new Exception("Incorrect number of callbacks detected."));
        } else {
          deferredDone.callback(null);
        }
      }

      // Schedule the next tick.
      else {
        i++;
        reactor.runTimerOneShot(this, 50, null);
      }
    }

    /*
     * Check for deferred callbacks.
     */
    public Object onCallback(Deferred deferred, Object data) {
      callbackCount++;
      return null;
    }

    /*
     * Check for deferred errbacks.
     */
    public Object onErrback(Deferred deferred, Exception error) {
      callbackError = error;
      return null;
    }
  }

  /**
   * Test deferred callback splitter with early trigger. Legacy test harness does
   * not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredSplitterEarlyCallback implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Splitter : Testing early callback.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredSplitterTestFixture callbackTest = new DeferredSplitterTestFixture(reactor, deferredDone, 0, false, 5);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test deferred callback splitter with late trigger. Legacy test harness does
   * not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredSplitterLateCallback implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Splitter : Testing late callback.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredSplitterTestFixture callbackTest = new DeferredSplitterTestFixture(reactor, deferredDone, 5, false, 5);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test deferred callback splitter with interim trigger. Legacy test harness
   * does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredSplitterInterimCallback implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Splitter : Testing interim callback.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredSplitterTestFixture callbackTest = new DeferredSplitterTestFixture(reactor, deferredDone, 2, false, 5);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test deferred callback splitter with early errback trigger. Legacy test
   * harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredSplitterEarlyErrback implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Splitter : Testing early errback.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredSplitterTestFixture callbackTest = new DeferredSplitterTestFixture(reactor, deferredDone, 0, true, 5);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test deferred callback splitter with late errback trigger. Legacy test
   * harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredSplitterLateErrback implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Splitter : Testing late errback.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredSplitterTestFixture callbackTest = new DeferredSplitterTestFixture(reactor, deferredDone, 5, true, 5);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

  /**
   * Test deferred callback splitter with interim errback trigger. Legacy test
   * harness does not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class DeferredSplitterInterimErrback implements Timeable {
    public void onTick(Object data) {
      System.out.println("Deferred Callback Splitter : Testing interim errback.");
      Object[] params = (Object[]) data;
      Reactor reactor = (Reactor) params[0];
      Deferred deferredDone = (Deferred) params[1];
      DeferredSplitterTestFixture callbackTest = new DeferredSplitterTestFixture(reactor, deferredDone, 2, true, 5);
      try {
        reactor.runTimerOneShot(callbackTest, 0, null);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }
  }

}
