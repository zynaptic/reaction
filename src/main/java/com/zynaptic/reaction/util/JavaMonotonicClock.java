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

package com.zynaptic.reaction.util;

/**
 * Implements a monotonic clock source using the standard Java monotonic clock.
 * This monotonic clock variant makes use of the <code>nanoTime</code> system
 * call which provides a monotonic clock source on supported platforms. Note
 * that some platforms, including many standard Linux distributions, do not
 * provide native monotonic clock support and these should use the fixed up
 * monotonic clock source instead.
 * <p>
 * This simple implementation does not wrap correctly and will fail after 292
 * years of uptime.
 * 
 * @author Chris Holgate
 */
public final class JavaMonotonicClock implements MonotonicClockSource {

  // Used to offset the clock so that it represents the reactor uptime.
  private long timeOffset = 0;

  /*
   * Implements MonotonicClockSource.getMsTime()
   */
  public synchronized long getMsTime() {
    return (System.nanoTime() - timeOffset) / 1000000;
  }

  /*
   * Implements MonotonicClockSource.init()
   */
  public synchronized void init() {
    timeOffset = System.nanoTime();
  }
}
