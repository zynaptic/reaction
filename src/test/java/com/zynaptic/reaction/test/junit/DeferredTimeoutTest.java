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

import com.zynaptic.reaction.test.core.DeferredTimeoutTests;
import com.zynaptic.reaction.test.junit.JUnitTestRunner;

/**
 * Test case for checking the timeout behaviour of the standard Deferred class.
 * 
 * @author Chris Holgate
 */
public class DeferredTimeoutTest extends TestCase {

  /**
   * Test deferred timeouts (normal operation).
   */
  public void testBasicDeferredTimeout() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredTimeoutTests.BasicDeferredTimeout());
  }

  /**
   * Test deferred timeouts (immediate effect).
   */
  public void testImmediateDeferredTimeout() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredTimeoutTests.ImmediateDeferredTimeout());
  }

  /**
   * Test deferred timeouts (with cancellation).
   */
  public void testCancelledDeferredTimeout() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredTimeoutTests.CancelledDeferredTimeout());
  }
}