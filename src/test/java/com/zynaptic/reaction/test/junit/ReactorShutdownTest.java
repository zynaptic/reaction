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

import java.lang.reflect.Field;
import java.util.Set;
import java.util.Map;
import java.util.List;
import junit.framework.Assert;
import junit.framework.TestCase;

import com.zynaptic.reaction.Deferrable;
import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.Reactor;
import com.zynaptic.reaction.Signal;
import com.zynaptic.reaction.Signalable;
import com.zynaptic.reaction.Threadable;
import com.zynaptic.reaction.Timeable;
import com.zynaptic.reaction.ReactorNotRunningException;
import com.zynaptic.reaction.core.ReactorControl;
import com.zynaptic.reaction.core.ReactorCore;
import com.zynaptic.reaction.util.FixedUpMonotonicClock;
import com.zynaptic.reaction.util.MonotonicClockSource;
import com.zynaptic.reaction.util.ReactorLogTarget;
import com.zynaptic.reaction.util.ReactorLogSystemOut;

/**
 * This class encapsulates the reactor shutdown tests. Since all these tests
 * require explicit control over the reactor state they are only available as
 * part of the POJO JUnit test suite. Legacy test harness does not include
 * strong generic type checking.
 * 
 * @author Chris Holgate
 */
@SuppressWarnings("unchecked")
public class ReactorShutdownTest extends TestCase {

  // Use fixed up wallclock as reactor timebase.
  private MonotonicClockSource reactorClock = new FixedUpMonotonicClock();

  // Use logging to System.out.
  private ReactorLogTarget logService = new ReactorLogSystemOut();

  // Local handle on the reactor.
  private Reactor reactor = ReactorCore.getReactor();

  // Local handle on the reactor control interface.
  private ReactorControl reactorControl = ReactorCore.getReactorControl();

  // Count number of thread cancellations.
  private int threadCancellations = 0;

  /*
   * Check internal state of the reactor after shutdown.
   */
  @SuppressWarnings("rawtypes")
  private void checkReactorShutdownState() {
    System.out.println("  Checking reactor shutdown state.");
    Field fld;
    Class cls = reactor.getClass();

    try {
      // Check state of reactor running flag.
      fld = cls.getDeclaredField("reactorRunning");
      fld.setAccessible(true);
      boolean reactorRunning = fld.getBoolean(reactor);
      Assert.assertEquals("reactorRunning - invalid shutdown state.", reactorRunning, false);

      // Check that the shutdown signal has been cleared.
      fld = cls.getDeclaredField("shutdownSignal");
      fld.setAccessible(true);
      Signal shutdownSignal = (Signal) fld.get(reactor);
      Assert.assertNull("shutdownSignal - invalid shutdown state.", shutdownSignal);

      // Check that the signal queue is empty.
      fld = cls.getDeclaredField("signalQueue");
      fld.setAccessible(true);
      List signalQueue = (List) fld.get(reactor);
      Assert.assertEquals("signalQueue - invalid shutdown state.", 0, signalQueue.size());

      // Check that the ordered timer set is empty.
      fld = cls.getDeclaredField("timerSet");
      fld.setAccessible(true);
      Set timerSet = (Set) fld.get(reactor);
      Assert.assertEquals("timerSet - invalid shutdown state.", 0, timerSet.size());

      // Check that the timer hash map is empty.
      fld = cls.getDeclaredField("timerMap");
      fld.setAccessible(true);
      Map timerMap = (Map) fld.get(reactor);
      Assert.assertEquals("timerMap - invalid shutdown state.", 0, timerMap.size());

      // Check that the deferred queue is empty.
      fld = cls.getDeclaredField("deferredQueue");
      fld.setAccessible(true);
      List deferredQueue = (List) fld.get(reactor);
      Assert.assertEquals("deferredQueue - invalid shutdown state.", 0, deferredQueue.size());

      // Check that the idle threads list is empty.
      fld = cls.getDeclaredField("idleThreads");
      fld.setAccessible(true);
      List idleThreads = (List) fld.get(reactor);
      Assert.assertEquals("idleThreads - invalid shutdown state.", 0, idleThreads.size());

      // Check that the running threads map is empty.
      fld = cls.getDeclaredField("runningThreads");
      fld.setAccessible(true);
      Map runningThreads = (Map) fld.get(reactor);
      Assert.assertEquals("runningThreads - invalid shutdown state.", 0, runningThreads.size());

      // Check that the completed threads map is empty.
      fld = cls.getDeclaredField("completedThreads");
      fld.setAccessible(true);
      Map completedThreads = (Map) fld.get(reactor);
      Assert.assertEquals("completedThreads - invalid shutdown state.", 0, completedThreads.size());

    } catch (Exception error) {
      error.printStackTrace();
      Assert.fail("Unexpected exception in reactor reflection.");
    }
  }

  /*
   * Threadable object used for killed threading test.
   */
  @SuppressWarnings("rawtypes")
  private class ThreadableKilledCall implements Timeable, Threadable, Deferrable {

    private int i = 0;
    private String magicData1 = "Test data 1";
    private Exception magicError1 = new Exception("Test Exception 1");

    /*
     * Timer tick function - called on reactor startup and used to submit the
     * threadable to the reactor. Shuts down reactor on second callback.
     */
    public void onTick(Object data) {
      if (i == 0) {
        System.out.println("  Submitting threadable.");
        try {
          Deferred deferred = reactor.runThread(this, magicData1);
          deferred.addDeferrable(this, true);
          reactor.runTimerOneShot(this, 5000, null);
        } catch (Exception error) {
          Assert.fail("Failed to start thread : " + error.toString());
        }
      } else if (i == 1) {
        System.out.println("  Killing threadable by stopping reactor.");
        reactorControl.stop();
      }
      i++;
    }

    /*
     * Threadable method just waits for a bit then throws exception.
     */
    public Object run(Object data) throws Exception {
      System.out.println("  Executing threadable.");
      Assert.assertSame("Threadable parameter data mismatch", data, magicData1);
      Thread.sleep(10000);
      throw magicError1;
    }

    /*
     * Errbacks should not be fired - although some interrupted exceptions may get
     * through as a result of thread cancellation.
     */
    public Object onErrback(Deferred deferred, Exception error) throws Exception {
      if (!((error instanceof InterruptedException) || (error instanceof ReactorNotRunningException))) {
        Assert.fail("Unexpected errback detected.");
      } else {
        threadCancellations++;
      }
      return null;
    }

    /*
     * Callbacks should not be fired.
     */
    public Object onCallback(Deferred deferred, Object data) throws Exception {
      Assert.fail("Unexpected callback detected.");
      return null;
    }
  }

  /**
   * Test thread execution with thread killed before completion. This cannot be
   * tested in OSGi configuration, so uses a dedicated POJO test.
   */
  public void testThreadableKilledCall() {
    System.out.println("Reactor : Testing killed threadable execution.");
    reactorControl.start(reactorClock, logService);
    try {
      reactor.runTimerOneShot(new ThreadableKilledCall(), 0, null);
      reactorControl.join();
    } catch (Exception error) {
      Assert.fail(error.getStackTrace().toString());
    }
    checkReactorShutdownState();
  }

  /*
   * Signalable object used for reactor shutdown signal test.
   */
  @SuppressWarnings("rawtypes")
  private class ReactorShutdownSignal implements Timeable, Signalable {

    private int i = 0;
    private Signal shutdownSignal = reactor.getReactorShutdownSignal();
    public boolean signalReceived = false;

    /*
     * Timer tick function - called on reactor startup. Shuts down reactor on second
     * callback.
     */
    public void onTick(Object data) {
      if (i == 0) {
        System.out.println("  Registering for shutdown signal.");
        try {
          reactor.getReactorShutdownSignal().subscribe(this);
          reactor.runTimerOneShot(this, 1000, null);
        } catch (Exception error) {
          Assert.fail("Failed to start timer : " + error.toString());
        }
      } else if (i == 1) {
        System.out.println("  Stopping reactor.");
        reactorControl.stop();
      }
      i++;
    }

    /*
     * Reactor shutdown callback.
     */
    public void onSignal(Signal signalId, Object data) {
      System.out.println("  Got shutdown signal.");
      Assert.assertEquals("Unexpected signal ID in callback.", signalId, shutdownSignal);
      Assert.assertEquals("Expected integer zero in shutdown signal callback.", ((Integer) data).intValue(), 0);
      signalReceived = true;
    }
  }

  /**
   * Reactor shutdown signal test. Checks that the reactor emits the shutdown
   * signal when stopped.
   */
  public void testReactorShutdownSignal() {
    System.out.println("Reactor : Testing reactor shutdown signal.");
    reactorControl.start(reactorClock, logService);
    ReactorShutdownSignal testFixture = new ReactorShutdownSignal();
    try {
      reactor.runTimerOneShot(testFixture, 0, null);
      reactorControl.join();
    } catch (Exception error) {
      Assert.fail(error.getStackTrace().toString());
    }
    Assert.assertEquals("No reactor shutdown signal received.", testFixture.signalReceived, true);
    checkReactorShutdownState();
  }

  /**
   * Multiple thread cancel and kill test. This test exposes a bug in early
   * reactor implementations, whereby killing the reactor with at least one
   * threadable in the process of completing could cause a deadlock.
   */
  public void testNoCancelKillDeadlock() {
    System.out.println("Reactor : Testing deadlock avoidance in thread cancel/kill.");
    reactorControl.start(reactorClock, logService);
    threadCancellations = 0;
    ThreadableKilledCall[] threadables = new ThreadableKilledCall[10];
    try {
      for (int i = 0; i < 10; i++) {
        threadables[i] = new ThreadableKilledCall();
        reactor.runTimerOneShot(threadables[i], 0, null);
      }
      Thread.sleep(1000);
      for (int i = 0; i < 10; i++) {
        reactor.cancelThread(threadables[i]);
      }
      reactorControl.stop();
      reactorControl.join();
    } catch (Exception error) {
      Assert.fail(error.getStackTrace().toString());
    }
    Assert.assertEquals("Incorrect number of thread cancellations", 10, threadCancellations);
    checkReactorShutdownState();
  }
}