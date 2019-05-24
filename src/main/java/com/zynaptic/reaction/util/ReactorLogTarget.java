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

import java.util.MissingResourceException;

import com.zynaptic.reaction.Logger;

/**
 * Defines the API used by the Reaction package to access message loggers for a
 * suitable log service. For example, standard POJO environments can use this
 * interface to wrap <code>java.util.logging</code> and OSGi environments can
 * use it to wrap <code>org.osgi.service.log</code>.
 * 
 * @author Chris Holgate
 */
public interface ReactorLogTarget {

  /**
   * Gets the runtime message logger which is associated with the specified logger
   * ID. If a message logger for the specified logger ID already exists this will
   * be returned to the caller. If no message logger for the specified logger ID
   * currently exists a new logger object will be created and returned.
   * 
   * @param loggerId This is the logger ID which is associated with the requested
   *   message logger. This is a String value which will normally hold the
   *   canonical name of the client class which is using the logger.
   * @param loggerResources This is the name of a ResourceBundle which is to be
   *   used for localising messages for this logger. May be null if none of the
   *   messages require localisation.
   * @return Returns a logger object which may be used for logging runtime
   *   messages.
   * @throws MissingResourceException This exception will be thrown if a set of
   *   logger resources have been specified but cannot be found.
   */
  public Logger getLogger(String loggerId, String loggerResources) throws MissingResourceException;

}