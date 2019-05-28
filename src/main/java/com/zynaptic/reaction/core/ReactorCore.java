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

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.TreeSet;
import java.util.logging.Level;

import com.zynaptic.reaction.Deferrable;
import com.zynaptic.reaction.Deferred;
import com.zynaptic.reaction.DeferredConcentrator;
import com.zynaptic.reaction.DeferredContextException;
import com.zynaptic.reaction.DeferredSplitter;
import com.zynaptic.reaction.DeferredTimedOutException;
import com.zynaptic.reaction.Logger;
import com.zynaptic.reaction.Reactor;
import com.zynaptic.reaction.ReactorNotRunningException;
import com.zynaptic.reaction.Signal;
import com.zynaptic.reaction.Threadable;
import com.zynaptic.reaction.ThreadableRunningException;
import com.zynaptic.reaction.Timeable;
import com.zynaptic.reaction.util.MonotonicClockSource;
import com.zynaptic.reaction.util.ReactorLogTarget;

/**
 * Implements the main reactor functionality. This class defines the singleton
 * object which provides all the core functionality of the reactor service. It
 * exposes the user API via the {@link com.zynaptic.reaction.Reactor} interface
 * and the reactor control API via the {@link ReactorControl} interface.
 * 
 * @author Chris Holgate
 */
public final class ReactorCore implements Reactor, ReactorControl {

  // Set the maximum number of idle threads that are maintained.
  private static final int MAX_IDLE_THREADS = 5;

  // This is the singleton instance for the reactor.
  private static final ReactorCore instance = new ReactorCore();

  // Flag used to indicate that the reactor is running.
  private boolean reactorRunning = false;

  // Clock which provides the reactor timebase.
  private MonotonicClockSource clockSource;

  // Log service used for dispatching reactor log messages.
  private ReactorLogTarget logTarget;

  // Message logger used for logging reactor core messages.
  private Logger logger;

  // Handle on the reactor shutdown signal.
  private SignalCore<Integer> shutdownSignal = null;

  // List of signal events queued for processing.
  private final LinkedList<SignalData<?>> signalQueue = new LinkedList<SignalData<?>>();

  // Timer events are sorted using a tree set collection.
  private final TreeSet<TimerData<?>> timerSet = new TreeSet<TimerData<?>>();

  // Timer events are indexed using the identity of their timeable.
  private final IdentityHashMap<Timeable<?>, TimerData<?>> timerMap = new IdentityHashMap<Timeable<?>, TimerData<?>>();

  // List of deferreds queued for processing.
  private final LinkedList<DeferredCore<?>> deferredQueue = new LinkedList<DeferredCore<?>>();

  // Thread group used to run the threadables.
  private final ThreadGroup threadGroup = new ThreadGroup("Reaction");

  // Thread ID counter.
  private int threadIdCount = 1;

  // List of idle worker threads.
  private final LinkedList<ThreadContainer<?, ?>> idleThreads = new LinkedList<ThreadContainer<?, ?>>();

  // Set of running worker threads.
  private final IdentityHashMap<Threadable<?, ?>, ThreadContainer<?, ?>> runningThreads = new IdentityHashMap<Threadable<?, ?>, ThreadContainer<?, ?>>();

  // List of completed worker threads.
  private final IdentityHashMap<Threadable<?, ?>, ThreadContainer<?, ?>> completedThreads = new IdentityHashMap<Threadable<?, ?>, ThreadContainer<?, ?>>();

  // Main reactor thread.
  private Thread reactorThread = null;

  // Stores error conditions which cause the reactor to shut down.
  private Error exitError = null;

  /*
   * Private constructor for the ReactorCore class. This ensures that we can only
   * ever create the singleton object.
   */
  private ReactorCore() {
  }

  /**
   * Accesses the reactor user interface. This method is used in order to get a
   * handle on the user interface of the singleton reactor component.
   * 
   * @return Returns a handle on the reactor user interface.
   */
  public static Reactor getReactor() {
    return instance;
  }

  /**
   * Accesses the reactor control interface. This method is used in order to get a
   * handle on the control interface of the singleton reactor component.
   * 
   * @return Returns a handle on the reactor control interface.
   */
  public static ReactorControl getReactorControl() {
    return instance;
  }

  /*
   * Package scope method which allows other core components to get a handle on
   * the full ReactorCore object.
   */
  static ReactorCore getReactorCore() {
    return instance;
  }

  /*
   * Implements Reactor.start(...) with the reactor running at the maximum thread
   * priority.
   */
  public synchronized void start(final MonotonicClockSource clockSource, final ReactorLogTarget logTarget)
      throws ThreadableRunningException {

    // Only attempt to start the reactor thread if it is not already
    // running.
    if ((reactorThread != null) && (reactorThread.isAlive())) {
      throw new ThreadableRunningException("Reactor thread already running");
    }

    // Update references to reactor service objects.
    this.logTarget = logTarget;
    logger = logTarget.getLogger("com.zynaptic.reaction", null);
    logger.log(Level.INFO, "\n  Reaction Asynchronous Programming Framework (c)2009-2019 Zynaptic Ltd.\n"
        + "  For license terms see http://www.apache.org/licenses/LICENSE-2.0\n");
    this.clockSource = clockSource;
    this.clockSource.init();

    // Create a runnable object for kicking off the main reactor loop.
    Runnable runnable = new Runnable() {
      public void run() {
        runReactor();
      }
    };

    // Spawn the reactor thread.
    reactorRunning = true;
    exitError = null;
    reactorThread = new Thread(threadGroup, runnable, "Reactor");
    reactorThread.setPriority(Thread.MAX_PRIORITY);
    reactorThread.start();
    logger.log(Level.INFO, "Started reactor.");
  }

  /*
   * Implements Reactor.stop()
   */
  public synchronized void stop() {
    logger.log(Level.INFO, "Halting reactor.");
    reactorRunning = false;
    notifyAll();
  }

  /*
   * Implements Reactor.join()
   */
  public void join() throws InterruptedException {
    Thread activeThread;
    synchronized (this) {
      activeThread = reactorThread;
    }
    if ((activeThread != null) && (activeThread != Thread.currentThread())) {
      activeThread.join();
      logger.log(Level.INFO, "Reactor stopped.");
      if (exitError != null) {
        throw exitError;
      }
    }
  }

  /*
   * Implements Reactor.getUptime()
   */
  public synchronized long getUptime() {
    if (clockSource != null) {
      return clockSource.getMsTime();
    } else {
      return 0;
    }
  }

  /*
   * Implements Reactor.getLogger(...)
   */
  public synchronized Logger getLogger(String loggerId) {
    return logTarget.getLogger(loggerId, null);
  }

  /*
   * Implements Reactor.getLogger(...)
   */
  public synchronized Logger getLogger(String loggerId, String loggerResources) throws MissingResourceException {
    return logTarget.getLogger(loggerId, loggerResources);
  }

  /*
   * Implements Reactor.getReactorShutdownSignal()
   */
  public synchronized Signal<Integer> getReactorShutdownSignal() {
    if (shutdownSignal == null) {
      shutdownSignal = new SignalCore<Integer>();
    }
    return shutdownSignal.makeRestricted();
  }

  /*
   * Package scope method which allows signal objects to add themselves to the
   * signal queue.
   */
  synchronized <T> void signal(final SignalCore<T> signalId, final T data, final boolean isFinal)
      throws ReactorNotRunningException {

    // Check to see if the reactor is up.
    if (!reactorRunning) {
      throw new ReactorNotRunningException("Can't schedule signal unless reactor is runnng.");
    }

    // All good - add the signal to the reactor signal queue and wake the
    // reactor thread.
    else {
      signalQueue.add(new SignalData<T>(signalId, data, isFinal));
      notifyAll();
    }
  }

  /*
   * Implements Reactor.runThread(threadable, data)
   */
  public synchronized <T, U> Deferred<U> runThread(final Threadable<T, U> threadable, final T data)
      throws ReactorNotRunningException, ThreadableRunningException {

    // Check for null threadable parameter.
    if (threadable == null) {
      throw new NullPointerException("Null pointer passed as threadable parameter.");
    }

    // Check to see if the reactor is up.
    if (!reactorRunning) {
      throw new ReactorNotRunningException("Can't schedule threadable unless reactor is runnng.");
    }

    // Each threadable can only be running in one thread.
    if ((runningThreads.get(threadable) != null) || (completedThreads.get(threadable) != null)) {
      throw new ThreadableRunningException("Threadable already running.");
    }

    // If there is an idle thread available, use that. Otherwise create a
    // new one.
    ThreadContainer<T, U> threadContainer;
    Deferred<U> deferred = new DeferredCore<U>();
    if (idleThreads.isEmpty()) {
      threadContainer = new ThreadContainer<T, U>(threadGroup, "Worker" + threadIdCount++);
    } else {
      threadContainer = idleThreads.removeFirst().typePunned();
    }

    // Add the thread to the set of running threads, indexed by it's
    // threadable object.
    runningThreads.put(threadable, threadContainer);
    threadContainer.runThreadable(threadable, data, deferred);
    return deferred.makeRestricted();
  }

  /*
   * Implements Reactor.runThread(threadable, data, msTimeout)
   */
  public synchronized <T, U> Deferred<U> runThread(final Threadable<T, U> threadable, final T data, int msTimeout)
      throws ReactorNotRunningException, ThreadableRunningException {

    // Run the thread in a conventional manner and assign the timeout.
    Deferred<U> deferred = runThread(threadable, data).setTimeout(msTimeout);

    // Add a callback handler which automatically cancels the thread when the
    // deferred callback timeout fires.
    deferred.addDeferrable(new Deferrable<U, U>() {
      public U onCallback(Deferred<U> deferred, U data) {
        return data;
      }

      public U onErrback(Deferred<U> deferred, Exception error) throws Exception {
        if (error instanceof DeferredTimedOutException) {
          cancelThread(threadable);
        }
        throw error;
      }
    }, false);
    return deferred;
  }

  /*
   * Implements Reactor.cancelThread(...)
   */
  public synchronized void cancelThread(final Threadable<?, ?> threadable) {

    // Check for null threadable parameter.
    if (threadable == null) {
      throw new NullPointerException("Null pointer passed as threadable parameter.");
    }

    // Skip cancellation if reactor is already shutting down.
    if (reactorRunning) {
      ThreadContainer<?, ?> threadContainer = runningThreads.get(threadable);
      if (threadContainer != null) {
        threadContainer.cancelThreadable();
      }
    }
  }

  /*
   * This method is used to signal thread completion. It is called from threadpool
   * context and moves the specified thread from the running to the completed
   * state before signalling the main reactor thread.
   */
  private synchronized void threadCompletion(final ThreadContainer<?, ?> threadContainer) {
    runningThreads.remove(threadContainer.threadable);
    completedThreads.put(threadContainer.threadable, threadContainer);
    notifyAll();
  }

  /*
   * Implements Reactor.runTimerOneShot(...)
   */
  public <T> void runTimerOneShot(final Timeable<T> timeable, final int msDelay, final T data)
      throws ReactorNotRunningException {
    runTimerRepeating(timeable, msDelay, 0, data);
  }

  /*
   * Implements Reactor.runTimerRepeating(...)
   */
  public synchronized <T> void runTimerRepeating(final Timeable<T> timeable, final int msDelay, final int msInterval,
      T data) throws ReactorNotRunningException {

    // Check for null pointer references for timeable parameter.
    if (timeable == null) {
      throw new NullPointerException("Null pointer passed as timeable parameter.");
    }

    // Check to see if the reactor is up.
    if (!reactorRunning) {
      throw new ReactorNotRunningException("Can't set timer unless reactor is runnng.");
    }

    // If the timeable is already associated with a timer, update it.
    TimerData<?> timer = timerMap.get(timeable);
    if (timer != null) {
      timerSet.remove(timer);
    }

    // If the timeable is not already associated with a timer, create one.
    else {
      timer = new TimerData<T>(timeable);
      timerMap.put(timeable, timer);
    }

    // Set the timer fields and add it back to the ordered set. Wake the reactor
    // loop once the timer is set up so that it can adjust its wait timeout
    // accordingly.
    timer.typePunned().data = data;
    timer.interval = (msInterval <= 0) ? 0 : msInterval;
    timer.trigger = clockSource.getMsTime() + ((msDelay <= 0) ? 0 : msDelay);
    timerSet.add(timer);
    notifyAll();
  }

  /*
   * Implements Reactor.cancelTimer(...)
   */
  public synchronized void cancelTimer(final Timeable<?> timeable) {

    // Check for null pointer references for timeable parameter.
    if (timeable == null) {
      throw new NullPointerException("Null pointer passed as timeable parameter.");
    }

    // Skip cancellation if the reactor is already shutting down.
    if (reactorRunning) {
      TimerData<?> timer = timerMap.get(timeable);
      if (timer != null) {
        timerSet.remove(timer);
        timerMap.remove(timeable);
      }
    }
  }

  /*
   * Implements Reactor.newDeferred()
   */
  public <T> Deferred<T> newDeferred() {
    return new DeferredCore<T>();
  }

  /*
   * Implements Reactor.callDeferred(...)
   */
  public <T> Deferred<T> callDeferred(T callbackData) {
    Deferred<T> deferred = new DeferredCore<T>();
    deferred.callback(callbackData);
    return deferred.makeRestricted();
  }

  /*
   * Implements Reactor.failDeferred(...)
   */
  public <T> Deferred<T> failDeferred(Exception error) {
    Deferred<T> deferred = new DeferredCore<T>();
    deferred.errback(error);
    return deferred.makeRestricted();
  }

  /*
   * Implements Reactor.newDeferredSplitter()
   */
  public <T> DeferredSplitter<T> newDeferredSplitter() {
    return new DeferredSplitterCore<T>();
  }

  /*
   * Implements Reactor.newDeferredConcentrator()
   */
  public <T> DeferredConcentrator<T> newDeferredConcentrator() {
    return new DeferredConcentratorCore<T>();
  }

  /*
   * Implements Reactor.newSignal()
   */
  public <T> Signal<T> newSignal() {
    return new SignalCore<T>();
  }

  /*
   * Schedule execution of deferred callback chain, waking the reactor if
   * required. If the reactor is not running, propagate the
   * ReactorNotRunningException instead.
   */
  synchronized void processDeferred(final DeferredCore<?> deferred) {
    if (reactorRunning) {
      deferredQueue.add(deferred);
      notifyAll();
    } else {
      deferred.processCallbackChain(false);
    }
  }

  /*
   * Provide error handling for deferred execution. This is always called from
   * reactor thread context on closing a deferred callback chain.
   */
  synchronized void closeDeferred(final DeferredCore<?> deferred, final Throwable error) {
    if (error != null) {
      if (error instanceof Error) {
        logger.log(Level.SEVERE, "Fatal error in closing deferred callback - stopping reactor.", error);
        exitError = (Error) error;
        reactorRunning = false;
      } else {
        logger.log(Level.WARNING, "Unhandled exception in closing deferred callback.", error);
      }
    }
  }

  /*
   * Ensures that the thread attempting to wait on a deferred event is not the
   * main reactor thread.
   */
  synchronized void checkDeferredContext() {
    if ((reactorThread == null) || (reactorThread == Thread.currentThread())) {
      throw new DeferredContextException(
          "Attempted to wait on a deferred event from within the main reactor thread context.");
    }
  }

  /*
   * Starts the main reactor loop in the context of the dedicated reactor thread.
   */
  private synchronized void runReactor() {

    // Runs until the reactor stop method clears the running flag.
    while (reactorRunning) {

      // Do a timed wait for the next event.
      if (deferredQueue.isEmpty() && signalQueue.isEmpty()) {
        try {
          if (!timerSet.isEmpty()) {
            long waitTimeout = (timerSet.first()).trigger - clockSource.getMsTime();
            if (waitTimeout > 0) {
              this.wait(waitTimeout);
            }
          } else {
            this.wait();
          }
        } catch (InterruptedException error) {
          logger.log(Level.SEVERE, "Main reactor thread interrupted - stopping reactor.", error);
          reactorRunning = false;
        }
      }

      // Process queued signals.
      processSignalQueue();

      // Process queued deferreds.
      processDeferredQueue();

      // Process completed threadables.
      processCompletedThreads();

      // Process expired timers.
      processExpiredTimers();
    }

    // Shutdown the reactor on reactor stop.
    reactorShutdown();
  }

  /*
   * Shut down the reactor cleanly on reactor stop request.
   */
  private void reactorShutdown() {

    // Cancel all running threads on reactor stop.
    Iterator<ThreadContainer<?, ?>> runningThreadIter = runningThreads.values().iterator();
    for (int i = 0; i < runningThreads.size(); i++) {
      runningThreadIter.next().cancelThreadable();
    }

    // Wait for running threads to complete, clearing the running threads list.
    while (runningThreads.size() != 0) {
      try {
        wait(100);
      } catch (InterruptedException error) {
        // Interruptions ignored since reactor is already shutting down.
      }
    }

    // This processes and clears the completed threads list.
    processCompletedThreads();

    // Kill all idle threads on reactor stop.
    Iterator<ThreadContainer<?, ?>> idleThreadIter = idleThreads.iterator();
    for (int i = 0; i < idleThreads.size(); i++) {
      idleThreadIter.next().killThread();
    }

    // Wait on all idle threads to die and clear the idle threads list.
    for (boolean threadRunning = false; threadRunning == true;) {
      try {
        wait(100);
      } catch (InterruptedException error) {
        logger.log(Level.SEVERE, "Main reactor thread interrupted during shutdown.", error);
      }
      idleThreadIter = idleThreads.iterator();
      for (int i = 0; i < idleThreads.size(); i++) {
        threadRunning |= idleThreadIter.next().threadRunning();
      }
    }
    idleThreads.clear();

    // Fire any outstanding deferreds.
    processDeferredQueue();

    // Issue any outstanding signals - including the reactor shutdown signal.
    if (shutdownSignal != null) {
      signalQueue.add(new SignalData<Integer>(shutdownSignal, Integer.valueOf(0), true));
      shutdownSignal = null;
    }
    processSignalQueue();

    // Cancel all timers on reactor stop.
    while (!timerSet.isEmpty()) {
      TimerData<?> timer = timerSet.first();
      timerSet.remove(timer);
      timerMap.remove(timer.timeable);
    }
  }

  /*
   * Fire any outstanding timer events.
   */
  private void processExpiredTimers() {
    while ((!timerSet.isEmpty()) && ((timerSet.first().trigger - clockSource.getMsTime()) <= 0)) {

      // Pop the timer from the front of the queue.
      TimerData<?> timer = timerSet.first();
      timerSet.remove(timer);

      // For one shot timers we remove the timer first so the timeable can
      // re-register itself.
      if (timer.interval <= 0) {
        timerMap.remove(timer.timeable);
      }

      // For repeating timers, update the trigger point. Note that in an
      // overloaded system the update interval is increased to give graceful
      // degradation.
      else {
        timer.trigger += timer.interval;
        while ((timer.trigger - clockSource.getMsTime()) <= 0) {
          logger.log(Level.WARNING, "Forced to merge " + timer.interval + "ms interval callbacks.");
          timer.trigger += timer.interval;
        }
        timerSet.add(timer);
      }

      // Fire the timer callback. Any exceptions are caught and logged
      // here to prevent them bringing down the reactor.
      try {
        timer.typePunned().timeable.onTick(timer.data);
      } catch (Exception error) {
        logger.log(Level.WARNING, "Unhandled exception in timer callback.", error);
      } catch (Error error) {
        logger.log(Level.SEVERE, "Fatal error in timer callback - stopping reactor.", error);
        exitError = error;
        reactorRunning = false;
      }
    }
  }

  /*
   * Walk the signal queue, issuing the signal events to all subscribed signalable
   * objects.
   */
  private void processSignalQueue() {
    while (!signalQueue.isEmpty()) {
      SignalData<?> signalData = signalQueue.pop();
      try {
        signalData.typePunned().signalId.processSignal(signalData.data, signalData.isFinal);
      } catch (Exception error) {
        logger.log(Level.WARNING, "Unhandled exception in signal execution.", error);
      } catch (Error error) {
        logger.log(Level.SEVERE, "Fatal error in signal execution - stopping reactor.", error);
        exitError = error;
        reactorRunning = false;
      }
    }
  }

  /*
   * Walk the deferred queue, firing deferreds. Any exceptions are caught and
   * logged here to prevent them bringing down the reactor.
   */
  private void processDeferredQueue() {
    while (!deferredQueue.isEmpty()) {
      DeferredCore<?> deferred = deferredQueue.pop();
      try {
        deferred.processCallbackChain(true);
      } catch (Exception error) {
        logger.log(Level.WARNING, "Unhandled exception in deferred execution.", error);
      } catch (Error error) {
        logger.log(Level.SEVERE, "Fatal error in deferred execution - stopping reactor.", error);
        exitError = error;
        reactorRunning = false;
      }
    }
  }

  /*
   * Process any outstanding completed threads.
   */
  private void processCompletedThreads() {
    Iterator<ThreadContainer<?, ?>> completedThreadIter = completedThreads.values().iterator();
    for (int i = 0; i < completedThreads.size(); i++) {
      ThreadContainer<?, ?> threadContainer = completedThreadIter.next();

      // Fire the deferred callbacks. The callback chains are not actually
      // executed at this stage.
      try {
        threadContainer.fireDeferred();
      } catch (Exception error) {
        logger.log(Level.WARNING, "Unhandled exception in thread completion.", error);
      } catch (Error error) {
        logger.log(Level.SEVERE, "Fatal error in thread completion - stopping reactor.", error);
        exitError = error;
        reactorRunning = false;
      }

      // Cull idle threads if required.
      if (idleThreads.size() < MAX_IDLE_THREADS) {
        idleThreads.addLast(threadContainer);
      } else {
        threadContainer.killThread();
      }
    }
    completedThreads.clear();
  }

  /*
   * Class used to store information about a single signal event.
   */
  private final class SignalData<T> {

    // The signal type identifier.
    final SignalCore<T> signalId;

    // The opaque data parameter associated with the signal event.
    final T data;

    // The flag indicating that the signal should be finalized.
    final boolean isFinal;

    /*
     * Initialise the signal data fields on construction.
     */
    SignalData(final SignalCore<T> signalId, final T data, final boolean isFinal) {
      this.signalId = signalId;
      this.data = data;
      this.isFinal = isFinal;
    }

    /*
     * Perform type punning so that a narrowing generic cast from a signal data
     * collection can be implemented without generic type warnings.
     */
    @SuppressWarnings("unchecked")
    <U> SignalData<U> typePunned() {
      return (SignalData<U>) this;
    }
  }

  /*
   * Class used to store information about a single timer event.
   */
  private final class TimerData<T> implements Comparable<TimerData<?>> {

    // The timeable object associated with this timer.
    final Timeable<T> timeable;

    // The opaque data parameter associated with this timer.
    T data;

    // Repeat interval. If this is set to zero the timer is one-shot.
    int interval;

    // Time at which timer will expire.
    long trigger;

    /*
     * Only initialise the final timeable field on construction.
     */
    TimerData(Timeable<T> timeable) {
      this.timeable = timeable;
    }

    /*
     * Perform type punning so that a narrowing generic cast from a timer data
     * collection can be implemented without generic type warnings.
     */
    @SuppressWarnings("unchecked")
    <U> TimerData<U> typePunned() {
      return (TimerData<U>) this;
    }

    /*
     * Timer objects are sorted according to their expiration times. This provides
     * the comparison method for sorting them. Note that when two objects have the
     * same trigger time the object identities are used to disambiguate them.
     */
    public int compareTo(TimerData<?> otherTimer) {
      if (this.trigger == otherTimer.trigger) {
        return System.identityHashCode(this) - System.identityHashCode(otherTimer);
      } else {
        return ((this.trigger - otherTimer.trigger) < 0) ? -1 : 1;
      }
    }
  }

  /*
   * Class used to wrap threads and their associated data. A number of these
   * objects are collected together to implement the threadpool.
   */
  private final class ThreadContainer<T, U> implements Runnable {

    // The thread object which is used to run the threadable.
    private final Thread thread;

    // Flag used to request execution of the threadable.
    private boolean runThread;

    // Flag used to request termination of the thread.
    private volatile boolean killThread;

    // Handle on the threadable which is being run.
    private volatile Threadable<T, U> threadable;

    // The opaque data objects used for parameter passing.
    private T dataIn;
    private U dataOut;

    // The exception object which is used to indicate errors.
    private Exception errData;

    // The deferred which is to be used for issuing callbacks.
    private Deferred<U> deferred;

    /*
     * Constructor used to start up the worker thread.
     */
    ThreadContainer(final ThreadGroup threadGroup, final String threadName) {
      runThread = false;
      killThread = false;
      thread = new Thread(threadGroup, this, threadName);
      thread.setPriority(Thread.NORM_PRIORITY);
      thread.start();
      logger.log(Level.FINE, "Started " + thread.toString());
    }

    /*
     * Perform type punning so that the same thread container object can be reused
     * with different type parameters, while maintaining generic type safety.
     */
    @SuppressWarnings("unchecked")
    <PT, PU> ThreadContainer<PT, PU> typePunned() {
      return (ThreadContainer<PT, PU>) this;
    }

    /*
     * Method used to initiate processing of a new threadable.
     */
    synchronized void runThreadable(final Threadable<T, U> threadable, final T data, final Deferred<U> deferred) {
      this.threadable = threadable;
      this.dataIn = data;
      this.deferred = deferred;
      runThread = true;
      notifyAll();
    }

    /*
     * Method used to cancel execution of the threadable. Note that this can be
     * called asynchronously to the executing thread.
     */
    void cancelThreadable() {
      Threadable<T, U> activeThreadable = threadable;
      logger.log(Level.FINE, "Cancelling " + thread.toString() + " (Running "
          + ((activeThreadable == null) ? "null" : activeThreadable.getClass().getName()) + ")");
      thread.interrupt();
    }

    /*
     * Method used to fire the deferred callback from within the reactor thread
     * context.
     */
    synchronized void fireDeferred() throws Exception {
      if (errData == null) {
        deferred.callback(dataOut);
      } else {
        deferred.errback(errData);
      }
    }

    /*
     * Runnable entry point for thread.
     */
    public synchronized void run() {
      while (!killThread) {

        // Wait for request to run new threadable.
        while (!runThread && !killThread) {
          try {
            wait();
          } catch (Exception error) {
            // Ignore spurious cancellations.
          }
        }

        // New threadable has been submitted - run it.
        try {
          if (!killThread) {
            dataOut = threadable.run(dataIn);
            errData = null;
          }
        }

        // Trap exceptions for passing up to deferred errback.
        catch (Exception error) {
          errData = error;
        }

        // Notify reactor of completion.
        if (!killThread) {
          runThread = false;
          threadCompletion(this);
          threadable = null;
        }
      }
    }

    /*
     * Termination request for thread on reactor shutdown. Note that this can be
     * called asynchronously to the executing thread.
     */
    void killThread() {
      logger.log(Level.FINE, "Killing " + thread.toString());
      killThread = true;
      thread.interrupt();
    }

    /*
     * Poll the thread to see if it is still running.
     */
    boolean threadRunning() {
      return thread.isAlive();
    }
  }
}
