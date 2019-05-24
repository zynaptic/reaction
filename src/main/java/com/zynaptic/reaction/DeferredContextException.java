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
 * Indicates an attempt to make an invalid call to the {@link Deferred#defer()}
 * method. This exception is thrown if an attempt is made to wait for a deferred
 * event callback from the context of the main reactor thread.
 * 
 * @author Chris Holgate
 */
public class DeferredContextException extends ReactionRuntimeException {
  private static final long serialVersionUID = -717342990131327098L;

  /**
   * Constructs a new deferred context exception. The only supported constructor
   * for this class of exception is the standard string message constructor.
   * 
   * @param msg This is the message string which is used to describe the error
   *   condition.
   */
  public DeferredContextException(String msg) {
    super(msg);
  }
}
