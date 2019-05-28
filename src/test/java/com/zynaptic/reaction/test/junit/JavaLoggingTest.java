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

import java.util.logging.Level;
import java.util.logging.LogManager;

import junit.framework.TestCase;

import com.zynaptic.reaction.Logger;
import com.zynaptic.reaction.Reactor;
import com.zynaptic.reaction.Timeable;
import com.zynaptic.reaction.core.ReactorControl;
import com.zynaptic.reaction.core.ReactorCore;
import com.zynaptic.reaction.util.FixedUpMonotonicClock;
import com.zynaptic.reaction.util.MonotonicClockSource;
import com.zynaptic.reaction.util.ReactorLogJavaStandard;
import com.zynaptic.reaction.util.ReactorLogTarget;

/**
 * This test is used to test the standard Java logging setup for the reactor
 * component.
 * 
 * @author Chris Holgate
 */
public class JavaLoggingTest extends TestCase {

  // Use fixed up wallclock as reactor timebase.
  private final MonotonicClockSource reactorClock = new FixedUpMonotonicClock();

  // Use logging to standard Java log service.
  private final ReactorLogTarget logService = new ReactorLogJavaStandard();

  // Local handle on the reactor.
  private final Reactor reactor = ReactorCore.getReactor();

  // Local handle on the reactor control interface.
  private final ReactorControl reactorControl = ReactorCore.getReactorControl();

  /**
   * Test standard Java logging support. This cannot be easily tested within the
   * OSGi framework tests, so uses a dedicated JUnit test fixture.
   */
  public final void testJavaLoggingSupport() {
    System.out.println("Reactor : Testing Java standard logging.");

    // Start the reactor component.
    reactorControl.start(reactorClock, logService);

    // Initiate the test.
    reactor.runTimerOneShot(new TestInitiator(), 1000, null);

    // Wait for test completion.
    try {
      reactorControl.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /*
   * Implement test harness logging.
   */
  private final class TestInitiator implements Timeable<Integer> {
    public void onTick(Integer data) {

      // Get a logger for this context.
      String loggerName = getClass().getCanonicalName();
      System.out.println("Getting logger for : " + loggerName);
      Logger logger = reactor.getLogger(loggerName);
      System.out.println("Reported logger ID : " + logger.getLoggerId());

      // Set root handler to allow all log messages.
      LogManager.getLogManager().getLogger("").getHandlers()[0].setLevel(Level.ALL);

      // Test finest log level.
      logger.setLogLevel(Level.FINEST);
      generateLogMessages(logger);

      // Test config log level.
      logger.setLogLevel(Level.CONFIG);
      generateLogMessages(logger);

      reactorControl.stop();
    }

    /*
     * Generate standard log messages at all log levels.
     */
    private final void generateLogMessages(Logger logger) {
      logger.log(Level.FINEST, "Logging FINEST level.");
      logger.log(Level.FINER, "Logging FINER level.");
      logger.log(Level.FINE, "Logging FINE level.");
      logger.log(Level.CONFIG, "Logging CONFIG level.");
      logger.log(Level.INFO, "Logging INFO level.");
      logger.log(Level.WARNING, "Logging WARNING level.");
      logger.log(Level.SEVERE, "Logging SEVERE level.");
    }
  }
}
