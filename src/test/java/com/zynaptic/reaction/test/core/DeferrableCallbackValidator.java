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

package com.zynaptic.reaction.test.core;

import com.zynaptic.reaction.Deferrable;
import com.zynaptic.reaction.Deferred;

/**
 * Define a deferrable object which checks callback validity. Legacy test
 * harness does not include strong generic type checking.
 */
public class DeferrableCallbackValidator implements Deferrable<Object, Object> {

  private Object refData;
  private Object outData;
  private Exception refException;
  private Exception outException;
  protected boolean called = false;
  protected Exception returnError = null;

  /**
   * Constructor for deferrable callback validator. This sets the expected
   * callback or errback data and the exception or data which will be emitted. If
   * the emitted exception is non-null it will be thrown, otherwise the emitted
   * data will be passed back.
   * 
   * @param refException This is the exception which is expected to be passed on
   *   errback or null if a callback is expected.
   * @param refData This is the data which is expected to be passed on callback.
   * @param outException This is the exception which is to be generated on
   *   activation or null if no exception is to be generated.
   * @param outData This is the data which is to be passed back on activation.
   */
  public DeferrableCallbackValidator(Exception refException, Object refData, Exception outException, Object outData) {
    this.refData = refData;
    this.outData = outData;
    this.refException = refException;
    this.outException = outException;
  }

  /*
   * Callback checks validity of data passed in and either throws the exception or
   * passes back the output data.
   */
  public Object onCallback(Deferred<Object> deferred, Object data) throws Exception {
    called = true;
    if (refException != null) {
      returnError = new Exception("Callback not expected in this context.");
    } else if (!refData.equals(data)) {
      returnError = new Exception("Data mismatch on callback (expected `" + refData + "`, got `" + data + "`)");
    }
    if (outException != null) {
      throw outException;
    }
    return outData;
  }

  /*
   * Errback checks validity of exception passed in and either throws the
   * exception or passes back the output data.
   */
  public Object onErrback(Deferred<Object> deferred, Exception error) throws Exception {
    called = true;
    if (refException == null) {
      returnError = new Exception("Errback not expected in this context.");
    } else if (!refException.getMessage().equals(error.getMessage())) {
      returnError = new Exception("Exception mismatch on errback (expected `" + refException.getMessage() + "`, got `"
          + error.getMessage() + "`)");
    }
    if (outException != null) {
      throw outException;
    }
    return outData;
  }
}
