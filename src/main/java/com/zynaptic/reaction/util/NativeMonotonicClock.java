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
 * Provides the Java interface to a native monotonic clock source. This
 * monotonic clock variant makes use of a native monotonic clock source provided
 * by the underlying hardware. Since the implementation of the native clock
 * source will be platform specific, there is no standard native library to
 * accompany this class definition.
 * 
 * @author Chris Holgate
 */
public final class NativeMonotonicClock implements MonotonicClockSource {

  /*
   * Implements MonotonicClockSource.getMsTime()
   */
  public synchronized native long getMsTime();

  /*
   * Implements MonotonicClockSource.init()
   */
  public synchronized native void init();

}
