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
import com.zynaptic.reaction.test.core.MultiTimerTests;
import com.zynaptic.reaction.test.junit.JUnitTestRunner;

/**
 * Test cases for checking multiple reactor timers.
 * 
 * @author Chris Holgate
 */
public class MultiTimerTest extends TestCase {

  /**
   * Test one-shot timers with a range of timeout values.
   */
  public void testOneShotMultiRangedTimeouts() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new MultiTimerTests.RangedOneShotTimeouts());
  }

  /**
   * Test one-shot timers with common timeout values.
   */
  public void testOneShotMultiCommonTimeouts() throws InterruptedException {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new MultiTimerTests.CommonOneShotTimeouts());
  }

  /**
   * Test repeating timers with a range of timeout values.
   */
  public void testRepeatingMultiRangedTimeouts() throws InterruptedException {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new MultiTimerTests.RangedRepeatingTimeouts());
  }

  /**
   * Test repeating timers with common timeout values.
   */
  public void testRepeatingMultiCommonTimeouts() throws InterruptedException {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new MultiTimerTests.CommonRepeatingTimeouts());
  }
}