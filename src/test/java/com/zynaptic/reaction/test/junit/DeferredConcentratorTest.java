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

import junit.framework.TestCase;

import com.zynaptic.reaction.test.core.DeferredConcentratorTests;
import com.zynaptic.reaction.test.junit.JUnitTestRunner;

/**
 * Test case for checking functionality of the deferred callback splitter class.
 * 
 * @author Chris Holgate
 */
public class DeferredConcentratorTest extends TestCase {

  /**
   * Test deferred callback concentrator with early trigger.
   */
  public void testDeferredConcentratorEarlyCallback() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredConcentratorTests.DeferredConcentratorEarlyCallback());
  }

  /**
   * Test deferred callback concentrator with late trigger.
   */
  public void testDeferredConcentratorLateCallback() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredConcentratorTests.DeferredConcentratorLateCallback());
  }

  /**
   * Test deferred callback concentrator with interim trigger.
   */
  public void testDeferredConcentratorInterimCallback() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredConcentratorTests.DeferredConcentratorInterimCallback());
  }

  /**
   * Test deferred callback concentrator with early errback.
   */
  public void testDeferredConcentratorEarlyErrback() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredConcentratorTests.DeferredConcentratorEarlyErrback());
  }

  /**
   * Test deferred callback concentrator with late errback.
   */
  public void testDeferredConcentratorLateErrback() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredConcentratorTests.DeferredConcentratorLateErrback());
  }

  /**
   * Test deferred callback concentrator with early multiple errbacks.
   */
  public void testDeferredConcentratorEarlyMultipleErrbacks() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredConcentratorTests.DeferredConcentratorEarlyMultipleErrbacks());
  }

  /**
   * Test deferred callback concentrator with late multiple errbacks.
   */
  public void testDeferredConcentratorLateMultipleErrbacks() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredConcentratorTests.DeferredConcentratorLateMultipleErrbacks());
  }

  /**
   * Test deferred callback concentrator with spanning multiple errbacks.
   */
  public void testDeferredConcentratorSpanningMultipleErrbacks() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredConcentratorTests.DeferredConcentratorSpanningMultipleErrbacks());
  }

}
