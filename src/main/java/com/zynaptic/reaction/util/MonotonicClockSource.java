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
 * Specifies the interface to a generic monotonic clock source. This interface
 * defines the way in which a reactor component can access a suitable monotonic
 * clock for use as its timebase. Various underlying implementations may be
 * used, depending on the Java version in use and the underlying platform
 * support for monotonic clocks.
 * 
 * @author Chris Holgate
 * 
 */
public interface MonotonicClockSource {

  /**
   * Initialises the monotonic clock source. This method is called on startup by
   * the reactor in order to initialise the timebase clock. It resets the current
   * clock time value to 0 and then sets the timer counter running.
   */
  public void init();

  /**
   * Gets the elapsed time since the monotonic clock source was initialised. This
   * method is called in order to obtain the current time, defined as the integer
   * number of milliseconds since the monotonic clock source was initialised.
   * 
   * @return Returns the monotonic time since the clock was initialised, specified
   *   in milliseconds. Returns invalid data if the clock has not been
   *   initialised.
   */
  public long getMsTime();

}
