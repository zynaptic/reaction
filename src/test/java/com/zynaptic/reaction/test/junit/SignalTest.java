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
import com.zynaptic.reaction.test.core.SignalTests;
import com.zynaptic.reaction.test.junit.JUnitTestRunner;

/**
 * Test cases for checking functionality of the reactor signal handling.
 * 
 * @author Chris Holgate
 */
public class SignalTest extends TestCase {

  /**
   * Test single signal handling (normal operation).
   */
  public void testSingleSignalCallback() throws InterruptedException {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new SignalTests.SingleSignalCallback());
  }

  /**
   * Test correct exception generation.
   */
  public void testInvalidOperationExceptions() throws InterruptedException {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new SignalTests.InvalidOperationExceptions());
  }

  /**
   * Test final signal generation.
   */
  public void testFinalSignalCallback() throws InterruptedException {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new SignalTests.FinalSignalCallback());
  }

}