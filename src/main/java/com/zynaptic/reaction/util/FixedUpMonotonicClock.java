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
 * Implements a monotonic clock source derived from the standard Java wallclock.
 * This monotonic clock variant uses the standard Java millisecond clock which
 * is susceptible to changes made to the underlying wallclock. It works around
 * potential changes to the wallclock time by maintaining a clock offset which
 * is updated when the wallclock is observed to go backwards or jump too far
 * into the future.
 * <p>
 * To minimise timing errors when such discontinuities occur, a sleepy thread
 * makes regular calls to {@link #getMsTime() getMsTime}, since timed calls to
 * the thread <code>sleep</code> method are assumed to be independent of the
 * wallclock time. This places an upper limit on the interval between requests
 * to {@link #getMsTime() getMsTime}.
 * 
 * @author Chris Holgate
 * 
 */
public final class FixedUpMonotonicClock implements MonotonicClockSource {

  // This sets the maximum interval at which getMsTime() should be called.
  private static final int MAX_MS_INTERVAL = 1000;

  // Used to track the timer offset and the last timer sampling point.
  private long timeOffset, timeLastRead;

  /*
   * Implements MonotonicClockSource.getMsTime()
   */
  public synchronized long getMsTime() {
    long timeCurrent = System.currentTimeMillis() - timeOffset;
    long timeDelta = timeCurrent - timeLastRead;

    // Fix up clock if it has gone backwards.
    if (timeDelta < 0) {
      timeCurrent = timeLastRead;
      timeOffset += timeDelta;
    }

    // Fix up clock if it has jumped forwards.
    else if (timeDelta > MAX_MS_INTERVAL * 2) {
      timeCurrent = timeLastRead + MAX_MS_INTERVAL;
      timeOffset += timeDelta - MAX_MS_INTERVAL;
    }

    // Fixed up version of the current time.
    timeLastRead = timeCurrent;
    return timeLastRead;
  }

  /*
   * Implements MonotonicClockSource.init()
   */
  public synchronized void init() {
    timeLastRead = 0;
    timeOffset = System.currentTimeMillis();
    Thread thread = new Thread(new PeriodicRequestGenerator());
    thread.start();
  }

  /*
   * Implements periodic requests to the fixed up monotonic clock in order to
   * detect step changes in the underlying clock source.
   */
  private final class PeriodicRequestGenerator implements Runnable {
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        getMsTime();
        try {
          Thread.sleep(MAX_MS_INTERVAL);
        } catch (InterruptedException error) {
          // Handled using isInterrupted()
        }
      }
    }
  }
}
