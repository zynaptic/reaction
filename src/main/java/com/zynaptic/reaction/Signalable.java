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

/**
 * Defines the interface to a signalable object. Signalable objects implement
 * this interface as a means of receiving signal event notifications from the
 * reactor. This provides a mechanism for broadcasting signal events to multiple
 * listeners.
 * <p>
 * Note that objects implementing this interface must not attempt to override
 * the default 'equals' method since this is used as an identity test within the
 * reactor core.
 * 
 * @param <T> This type parameter specifies the type of signal data which can be
 *   accepted by the signalable object.
 * 
 * @author Chris Holgate
 */
public interface Signalable<T> {

  /**
   * Receives a signal callback from the reactor. This method is called whenever a
   * signal is generated to which the signalable object is subscribed.
   * 
   * @param signalId This is a handle on a signal event object which may be used
   *   to identify the source of the signal being raised.
   * @param data This is the signal data object which is passed as a parameter to
   *   the signal event. It should be treated as an immutable object and no
   *   attempt should be made to change the state of this object within the
   *   callback.
   */
  public void onSignal(Signal<T> signalId, final T data);

}