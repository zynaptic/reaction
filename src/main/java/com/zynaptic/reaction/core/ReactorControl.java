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

package com.zynaptic.reaction.core;

import com.zynaptic.reaction.ThreadableRunningException;
import com.zynaptic.reaction.util.MonotonicClockSource;
import com.zynaptic.reaction.util.ReactorLogTarget;

/**
 * Defines the API for controlling the reactor service. The reactor object is
 * always a singleton and this interface can only be accessed via its
 * {@link ReactorCore#getReactorControl() getReactorControl} static method.
 * 
 * @author Chris Holgate
 */
public interface ReactorControl {

  /**
   * Starts the reactor running. This method is called in order to start the main
   * reactor thread, using the supplied monotonic clock as its timebase. It is
   * called from a separate host thread and will return once the reactor thread
   * has started running.
   * 
   * @param clockSource This is a monotonic clock which will be used as the
   *   timebase for the reactor.
   * @param logTarget This is the logging service to which all reactor log
   *   messages are redirected.
   * @throws ThreadableRunningException This exception is thrown if there is
   *   already a reactor thread running. This indicates a programming error.
   */
  public void start(MonotonicClockSource clockSource, ReactorLogTarget logTarget) throws ThreadableRunningException;

  /**
   * Requests that the reactor stop running. This method is called in order to
   * stop the reactor from running. It may be called from any thread context,
   * including the main reactor thread. This method returns immediately and if the
   * calling thread wishes to wait for the reactor to shut down it should follow
   * it with a call to the {@link #join() join} method.
   */
  public void stop();

  /**
   * Waits for the reactor thread to exit. This method may be called from any
   * thread except the main reactor thread in order to wait for the reactor thread
   * to exit. It is typically called immediately after the host thread has called
   * the {@link #stop() stop} method.
   * 
   * @throws InterruptedException This exception is thrown if the calling thread
   *   is interrupted before the reactor shutdown is complete. An Error is thrown
   *   if the corresponding error condition caused the reactor to shut down.
   */
  public void join() throws InterruptedException;

}
