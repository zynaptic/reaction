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

package com.zynaptic.reaction.test.junit;

import junit.framework.Assert;

import com.zynaptic.reaction.Deferrable;
import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.Reactor;
import com.zynaptic.reaction.Timeable;
import com.zynaptic.reaction.core.ReactorCore;
import com.zynaptic.reaction.core.ReactorControl;
import com.zynaptic.reaction.util.FixedUpMonotonicClock;
import com.zynaptic.reaction.util.MonotonicClockSource;
import com.zynaptic.reaction.util.ReactorLogTarget;
import com.zynaptic.reaction.util.ReactorLogSystemOut;

/**
 * Wrapper used to set up common tests in the JUnit framework. Includes reactor
 * control and the Deferrable interface which is used for notifying the JUnit
 * framework of successful or failed text completions. Legacy test harness does
 * not include strong generic type checking.
 * 
 * @author Chris Holgate
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JUnitTestRunner implements Deferrable {

  // Use fixed up wallclock as reactor timebase.
  private static final MonotonicClockSource reactorClock = new FixedUpMonotonicClock();

  // Use logging to System.out.
  private static final ReactorLogTarget logService = new ReactorLogSystemOut();

  // Local handle on the reactor control interface.
  private static final ReactorControl reactorControl = ReactorCore.getReactorControl();

  // Local handle on the reactor user interface.
  private static final Reactor reactor = ReactorCore.getReactor();

  /**
   * Run the specified common test code.
   */
  public void runTest(Timeable test) {
    reactorControl.start(reactorClock, logService);
    Deferred deferredDone = reactor.newDeferred();
    deferredDone.addDeferrable(this, true);
    Object[] params = { reactor, deferredDone };
    try {
      reactor.runTimerOneShot(test, 0, params);
      reactorControl.join();
    } catch (Exception error) {
      error.printStackTrace();
      Assert.fail(error.getMessage());
    }
  }

  /**
   * Deferred callback implies successful completion. This method just stops the
   * reactor.
   */
  public Object onCallback(Deferred deferred, Object data) throws Exception {
    reactorControl.stop();
    return null;
  }

  /**
   * Errback implies failed completion. This method asserts then stops the
   * reactor.
   */
  public Object onErrback(Deferred deferred, Exception error) throws Exception {
    error.printStackTrace();
    Assert.fail(error.toString());
    reactorControl.stop();
    return null;
  }
}
