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
import com.zynaptic.reaction.Reactor;
import com.zynaptic.reaction.Threadable;
import com.zynaptic.reaction.Timeable;

/**
 * This aggregation class holds all the tests required to verify single thread
 * behaviour.
 * 
 * @author Chris Holgate
 */
public class ThreadTests {

  // Define acceptable error when doing timing checks.
  private static final int MAX_TIMING_ERROR = 250;

  /**
   * Implement basic threading test with valid return. Legacy test harness does
   * not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ThreadableBasicCall implements Timeable, Threadable, Deferrable {

    private Reactor reactor;
    private Deferred deferredDone;
    private long timeStamp;
    private String magicData1 = "Test data 1";
    private String magicData2 = "Test data 2";
    private Exception returnError = null;

    /*
     * Timer tick function - called on test startup and used to submit the
     * threadable to the reactor.
     */
    public void onTick(Object data) {
      System.out.println("Reactor : Testing single thread with valid completion.");
      Object[] params = (Object[]) data;
      reactor = (Reactor) params[0];
      deferredDone = (Deferred) params[1];
      timeStamp = System.currentTimeMillis();
      try {
        Deferred deferred = reactor.runThread(this, magicData1);
        deferred.addDeferrable(this, true);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }

    /*
     * Threadable method just waits for a bit then returns new data.
     */
    public Object run(Object data) throws Exception {
      System.out.println("  Executing threadable.");
      if (data != magicData1) {
        returnError = new Exception("Threadable parameter data mismatch.");
      }
      Thread.sleep(1000);
      return magicData2;
    }

    /*
     * Deferred callback should pass data back from thread.
     */
    public Object onCallback(Deferred deferred, Object data) throws Exception {
      System.out.println("  Got threadable return value.");
      long timeError = System.currentTimeMillis() - timeStamp - 1000;
      if ((timeError > MAX_TIMING_ERROR) || (timeError < -MAX_TIMING_ERROR)) {
        returnError = new Exception("Thread delay timing error detected.");
      }
      if (data != magicData2) {
        returnError = new Exception("Threadable return data mismatch.");
      }
      if (returnError != null) {
        deferredDone.errback(returnError);
      } else {
        deferredDone.callback(null);
      }
      return null;
    }

    /*
     * Errbacks should not be fired.
     */
    public Object onErrback(Deferred deferred, Exception error) throws Exception {
      deferredDone.errback(error);
      return null;
    }
  }

  /**
   * Implement basic thread test with error condition. Legacy test harness does
   * not include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ThreadableErroredCall implements Timeable, Threadable, Deferrable {

    private Reactor reactor;
    private Deferred deferredDone;
    private long timeStamp;
    private String magicData1 = "Test data 1";
    private Exception magicError1 = new Exception("Test Exception 1");
    private Exception returnError = null;

    /*
     * Timer tick function - called on reactor startup and used to submit the
     * threadable to the reactor.
     */
    public void onTick(Object data) {
      System.out.println("Reactor : Testing single thread with errored completion.");
      Object[] params = (Object[]) data;
      reactor = (Reactor) params[0];
      deferredDone = (Deferred) params[1];
      timeStamp = System.currentTimeMillis();
      try {
        Deferred deferred = reactor.runThread(this, magicData1);
        deferred.addDeferrable(this, true);
      } catch (Exception error) {
        deferredDone.errback(error);
      }
    }

    /*
     * Threadable method just waits for a bit then throws exception.
     */
    public Object run(Object data) throws Exception {
      System.out.println("  Executing errored threadable.");
      if (data != magicData1) {
        returnError = new Exception("Threadable parameter data mismatch.");
      }
      Thread.sleep(1000);
      throw magicError1;
    }

    /*
     * Deferred errback should pass exception back from thread.
     */
    public Object onErrback(Deferred deferred, Exception error) throws Exception {
      System.out.println("  Got threadable return exception.");
      long timeError = System.currentTimeMillis() - timeStamp - 1000;
      if ((timeError > MAX_TIMING_ERROR) || (timeError < -MAX_TIMING_ERROR)) {
        returnError = new Exception("Thread delay timing error detected.");
      }
      if (error != magicError1) {
        returnError = new Exception("Threadable return error mismatch.");
      }
      if (returnError != null) {
        deferredDone.errback(returnError);
      } else {
        deferredDone.callback(null);
      }
      return null;
    }

    /*
     * Callbacks should not be fired.
     */
    public Object onCallback(Deferred deferred, Object data) throws Exception {
      deferredDone.errback(new Exception("Unexpected callback detected."));
      return null;
    }
  }

  /**
   * Implement basic thread test with cancellation. Legacy test harness does not
   * include strong generic type checking.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class ThreadableCancelledCall implements Timeable, Threadable, Deferrable {

    private Reactor reactor;
    private Deferred deferredDone;
    private int i = 0;
    private long timeStamp;
    private String magicData1 = "Test data 1";
    private Exception magicError1 = new Exception("Test Exception 1");
    private Exception returnError = null;

    /*
     * Timer tick function - called on reactor startup and used to submit the
     * threadable to the reactor. Cancels thread on second callback.
     */
    public void onTick(Object data) {
      if (i == 0) {
        System.out.println("Reactor : Testing single thread with cancellation.");
        Object[] params = (Object[]) data;
        reactor = (Reactor) params[0];
        deferredDone = (Deferred) params[1];
        timeStamp = System.currentTimeMillis();
        try {
          Deferred deferred = reactor.runThread(this, magicData1);
          deferred.addDeferrable(this, true);
          reactor.runTimerOneShot(this, 1000, null);
        } catch (Exception error) {
          deferredDone.errback(error);
        }
      } else if (i == 1) {
        System.out.println("  Cancelling threadable.");
        reactor.cancelThread(this);
      }
      i++;
    }

    /*
     * Threadable method just waits for a bit then throws exception.
     */
    public Object run(Object data) throws Exception {
      System.out.println("  Executing threadable.");
      if (data != magicData1) {
        returnError = new Exception("Threadable parameter data mismatch");
      }
      Thread.sleep(10000);
      throw magicError1;
    }

    /*
     * Deferred errback should contain interrupted exception.
     */
    public Object onErrback(Deferred deferred, Exception error) throws Exception {
      System.out.println("  Got threadable return exception.");
      long timeError = System.currentTimeMillis() - timeStamp - 1000;
      if ((timeError > MAX_TIMING_ERROR) || (timeError < -MAX_TIMING_ERROR)) {
        returnError = new Exception("Thread delay timing error detected.");
      }
      if (!(error instanceof InterruptedException)) {
        returnError = new Exception("Threadable exception mismatch");
      }
      if (returnError != null) {
        deferredDone.errback(returnError);
      } else {
        deferredDone.callback(null);
      }
      return null;
    }

    /*
     * Callbacks should not be fired.
     */
    public Object onCallback(Deferred deferred, Object data) throws Exception {
      deferredDone.errback(new Exception("Unexpected callback detected."));
      return null;
    }
  }

}
