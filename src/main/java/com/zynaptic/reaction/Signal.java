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

package com.zynaptic.reaction;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Defines the signal event interface. This provides the user API for
 * manipulating signal event objects. Signal event objects can only be created
 * by using the {@link Reactor#newSignal() newSignal} method on the
 * {@link Reactor} interface.
 * 
 * @param <T> This type parameter specifies the type of the data object which
 *   will be passed each time the signal is triggered.
 * 
 * @author Chris Holgate
 */
public interface Signal<T> {

  /**
   * Subscribes a new signalable object. This method is used in order to subscribe
   * a signalable object to this signal, which means that the signalable object
   * will be notified of any events associated with the signal. The signalable
   * object is subscribed with a priority level of zero.
   * 
   * @param signalable This is the signalable object which is being subscribed.
   * @throws SignalContextException This runtime exception is thrown if there is
   *   an attempt to subscribe a new signalable object from the context of a
   *   signal event callback.
   * @throws ReactorNotRunningException This exception is thrown when attempt is
   *   made to subscribe to a signal when the reactor is not running.
   */
  public void subscribe(Signalable<T> signalable) throws SignalContextException, ReactorNotRunningException;

  /**
   * Subscribes a new signalable object with a specific priority level. This
   * method is used in order to subscribe a signalable object to this signal,
   * which means that the signalable object will be notified of any events
   * associated with the signal. The signalable object is subscribed with the
   * specified priority level. On a signal event, signalable objects will be
   * notified in order of decreasing priority.
   * 
   * @param signalable This is the signalable object which is being subscribed.
   * @param priorityLevel This is the priority level which will be used to
   *   determine the notification order for subscribed signalable objects.
   *   Signalable objects with higher priority levels will be notified of signal
   *   events before those with lower priority levels.
   * @throws SignalContextException This runtime exception is thrown if there is
   *   an attempt to subscribe a new signalable object from the context of a
   *   signal event callback.
   * @throws ReactorNotRunningException This exception is thrown when attempt is
   *   made to subscribe to a signal when the reactor is not running.
   */
  public void subscribe(Signalable<T> signalable, int priorityLevel)
      throws SignalContextException, ReactorNotRunningException;

  /**
   * Subscribes a new consumer function without a signal identifier input. This
   * method is used in order to subscribe a consumer function to this signal,
   * which means that the consumer function will be called on of any events
   * associated with the signal. The consumer function is subscribed with a
   * priority level of zero.
   * 
   * @param consumer This is the consumer function which is being subscribed. It
   *   takes a single input which is the generated signal data.
   * @return Returns a reference to the signalable object which has been used to
   *   wrap the consumer function.
   * @throws SignalContextException This runtime exception is thrown if there is
   *   an attempt to subscribe a new consumer function from the context of a
   *   signal event callback.
   * @throws ReactorNotRunningException This exception is thrown when attempt is
   *   made to subscribe to a signal when the reactor is not running.
   */
  public Signalable<T> subscribe(Consumer<T> consumer) throws SignalContextException, ReactorNotRunningException;

  /**
   * Subscribes a new consumer function with a signal identifier input. This
   * method is used in order to subscribe a consumer function to this signal,
   * which means that the consumer function will be called on of any events
   * associated with the signal. The consumer function is subscribed with a
   * priority level of zero. Note that consumer functions cannot be unsubscribed
   * and will remain attached to the signal source until {@link #signalFinalize}
   * is called.
   * 
   * @param consumer This is the consumer function which is being subscribed. It
   *   takes two inputs, the first of which is a reference to the generating
   *   signal object and the second of which is the generated signal data.
   * @return Returns a reference to the signalable object which has been used to
   *   wrap the consumer function.
   * @throws SignalContextException This runtime exception is thrown if there is
   *   an attempt to subscribe a new consumer function from the context of a
   *   signal event callback.
   * @throws ReactorNotRunningException This exception is thrown when attempt is
   *   made to subscribe to a signal when the reactor is not running.
   */
  public Signalable<T> subscribe(BiConsumer<Signal<T>, T> consumer)
      throws SignalContextException, ReactorNotRunningException;

  /**
   * Unsubscribes a signalable object. This method is used in order to unsubscribe
   * a signalable object so that it no longer receives event notifications
   * associated with the signal.
   * 
   * @param signalable This is the signalable object which is being unsubscribed.
   * @throws SignalContextException This runtime exception is thrown if there is
   *   an attempt to unsubscribe a signalable object from the context of a signal
   *   event callback.
   * @throws ReactorNotRunningException This exception is thrown when attempt is
   *   made to unsubscribe from a signal when the reactor is not running.
   */
  public void unsubscribe(Signalable<T> signalable) throws SignalContextException, ReactorNotRunningException;

  /**
   * Sends a signal event notification. This method is used in order to trigger
   * the signal event. When called the event will be broadcast to all subscribed
   * signalable objects.
   * 
   * @param data This is the signal data parameter which will be passed to each
   *   subscribed signalable object in turn. It is good practice to make this
   *   parameter object immutable.
   * @throws RestrictedCapabilityException This exception is thrown if an attempt
   *   is made to call this method on a signal event object reference with
   *   restricted capability.
   * @throws ReactorNotRunningException This exception is thrown if an attempt is
   *   made to signal an event when the reactor is not running.
   */
  public void signal(final T data) throws RestrictedCapabilityException, ReactorNotRunningException;

  /**
   * Sends a finalising signal event notification. Finalising signals are
   * broadcast to all subscribers in the same way as for a conventional signal
   * event, after which all subscribers are automatically unsubscribed from the
   * signal. This will typically be used to notify shutdown of a service.
   * 
   * @param data This is the signal data parameter which will be passed to each
   *   subscribed signalable object in turn. It is good practice to make this
   *   parameter object immutable.
   * @throws RestrictedCapabilityException This exception is thrown if an attempt
   *   is made to call this method on a signal event object reference with
   *   restricted capability.
   * @throws ReactorNotRunningException This exception is thrown if an attempt is
   *   made to signal an event when the reactor is not running.
   */
  public void signalFinalize(final T data) throws RestrictedCapabilityException, ReactorNotRunningException;

  /**
   * Convert the signal interface to restricted capability. This method should be
   * used to restrict the capability of a signal interface so that the
   * {@link #signal(Object) signal} and {@link #signalFinalize(Object)
   * signalFinalize} methods are protected from unauthorised use.
   * 
   * @return Returns a version of the current {@link Signal} interface with
   *   restricted capability.
   */
  public Signal<T> makeRestricted();

}