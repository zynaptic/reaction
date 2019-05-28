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

import com.zynaptic.reaction.test.core.DeferredBasicTests;
import com.zynaptic.reaction.test.junit.JUnitTestRunner;

/**
 * Test cases for checking functionality of the standard Deferred class.
 * 
 * @author Chris Holgate
 */
public class DeferredBasicTest extends TestCase {

  /**
   * Test chained deferrable callbacks with late trigger.
   */
  public void testChainedCallbacksLateTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredBasicTests.ChainedCallbacksLateTrigger());
  }

  /**
   * Test chained deferrable callbacks with early trigger.
   */
  public void testChainedCallbacksEarlyTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredBasicTests.ChainedCallbacksEarlyTrigger());
  }

  /**
   * Test chained deferrable callbacks with interim trigger.
   */
  public void testChainedCallbacksInterimTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredBasicTests.ChainedCallbacksInterimTrigger());
  }

  /**
   * Test chained deferrable error callbacks with late trigger.
   */
  public void testChainedErrbacksLateTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredBasicTests.ChainedErrbacksLateTrigger());
  }

  /**
   * Test chained deferrable error callbacks with early trigger.
   */
  public void testChainedErrbacksEarlyTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredBasicTests.ChainedErrbacksEarlyTrigger());
  }

  /**
   * Test chained deferrable error callbacks with interim trigger.
   */
  public void testChainedErrbacksInterimTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredBasicTests.ChainedErrbacksInterimTrigger());
  }

  /**
   * Test chained interleaved callbacks with late trigger.
   */
  public void testChainedInterleavedLateTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredBasicTests.ChainedInterleavedLateTrigger());
  }

  /**
   * Test chained interleaved callbacks with early trigger.
   */
  public void testChainedInterleavedEarlyTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredBasicTests.ChainedInterleavedEarlyTrigger());
  }

  /**
   * Test chained interleaved callbacks with interim trigger.
   */
  public void testChainedInterleavedInterimTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredBasicTests.ChainedInterleavedInterimTrigger());
  }

  /**
   * Test to check exception generation on multiple callbacks.
   */
  public void testMultipleCallbackException() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredBasicTests.MultipleCallbackTest());
  }

  /**
   * Test to check exception generation on multiple errbacks.
   */
  public void testMultipleErrbackException() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredBasicTests.MultipleErrbackTest());
  }

  /**
   * Test to check exception generation on multiple terminal deferrables.
   */
  public void testMultipleTerminationException() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredBasicTests.MultipleTerminationTest());
  }
}
