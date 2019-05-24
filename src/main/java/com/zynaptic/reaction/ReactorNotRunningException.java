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
 * Indicates an attempt to access the reactor when it is not running. This
 * exception is thrown if an attempt is made to access the reactor while it is
 * in its stopped state. This is implemented as a runtime exception, since it
 * implies a bug whereby a client of the reactor has not been correctly
 * monitoring the reactor state.
 * 
 * @author Chris Holgate
 */
public class ReactorNotRunningException extends ReactionRuntimeException {
  private static final long serialVersionUID = -106155811856892741L;

  /**
   * Constructs a new reactor not running exception. The only supported
   * constructor for this class of exception is the standard string message
   * constructor.
   * 
   * @param msg This is the message string which is used to describe the error
   *   condition.
   */
  public ReactorNotRunningException(String msg) {
    super(msg);
  }
}
