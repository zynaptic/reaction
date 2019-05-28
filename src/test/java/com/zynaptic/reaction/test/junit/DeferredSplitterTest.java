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

import com.zynaptic.reaction.test.core.DeferredSplitterTests;
import com.zynaptic.reaction.test.junit.JUnitTestRunner;

/**
 * Test case for checking functionality of the deferred callback splitter class.
 * 
 * @author Chris Holgate
 */
public class DeferredSplitterTest extends TestCase {

  /**
   * Test deferred callback splitter with early trigger.
   */
  public void testDeferredSplitterEarlyCallback() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredSplitterTests.DeferredSplitterEarlyCallback());
  }

  /**
   * Test deferred callback splitter with late trigger.
   */
  public void testDeferredSplitterLateCallback() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredSplitterTests.DeferredSplitterLateCallback());
  }

  /**
   * Test deferred callback splitter with interim trigger.
   */
  public void testDeferredSplitterInterimCallback() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredSplitterTests.DeferredSplitterInterimCallback());
  }

  /**
   * Test deferred callback splitter with early errback trigger.
   */
  public void testDeferredSplitterEarlyErrback() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredSplitterTests.DeferredSplitterEarlyErrback());
  }

  /**
   * Test deferred callback splitter with late errback trigger.
   */
  public void testDeferrableListLateErrback() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredSplitterTests.DeferredSplitterLateErrback());
  }

  /**
   * Test deferred callback splitter with interim errback trigger.
   */
  public void testDeferrableListInterimErrback() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new DeferredSplitterTests.DeferredSplitterInterimErrback());
  }

}
