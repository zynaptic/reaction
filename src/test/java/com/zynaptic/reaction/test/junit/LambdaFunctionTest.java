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

import com.zynaptic.reaction.test.core.LambdaFunctionTests;

import junit.framework.TestCase;

/**
 * Test cases for checking functionality of the Lambda function API extensions.
 * 
 * @author Chris Holgate
 */
public class LambdaFunctionTest extends TestCase {

  /**
   * Test chained deferrable callbacks with early trigger.
   */
  public void testChainedCallbacksEarlyTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new LambdaFunctionTests.ChainedCallbacksEarlyTrigger());
  }

  /**
   * Test chained deferrable callbacks with late trigger.
   */
  public void testChainedCallbacksLateTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new LambdaFunctionTests.ChainedCallbacksLateTrigger());
  }

  /**
   * Test chained deferrable callbacks with interim trigger.
   */
  public void testChainedCallbacksInterimTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new LambdaFunctionTests.ChainedCallbacksInterimTrigger());
  }

  /**
   * Test chained deferrable error callbacks with early trigger.
   */
  public void testChainedErrbacksEarlyTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new LambdaFunctionTests.ChainedErrbacksEarlyTrigger());
  }

  /**
   * Test chained deferrable error callbacks with late trigger.
   */
  public void testChainedErrbacksLateTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new LambdaFunctionTests.ChainedErrbacksLateTrigger());
  }

  /**
   * Test chained deferrable error callbacks with interim trigger.
   */
  public void testChainedErrbacksInterimTrigger() {
    JUnitTestRunner testRunner = new JUnitTestRunner();
    testRunner.runTest(new LambdaFunctionTests.ChainedErrbacksInterimTrigger());
  }
}
