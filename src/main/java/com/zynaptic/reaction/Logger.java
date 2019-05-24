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

import java.util.logging.Level;

/**
 * Provides a common logging interface which may be used by Reaction clients to
 * perform runtime message logging to a common backend log service. Note that
 * the log levels correspond to the standard Java logging library levels.
 * 
 * @author Chris Holgate
 */
public interface Logger {

  /**
   * Logs a message string at the specified severity level.
   * 
   * @param level This is the severity level at which the message should be
   *   logged.
   * @param msg This is the message string which should be included in the log
   *   file.
   */
  public void log(Level level, String msg);

  /**
   * Logs message string and accompanying exception information at the specified
   * severity level.
   * 
   * @param level This is the severity level at which the message should be
   *   logged.
   * @param msg This is the message string which should be included in the log
   *   file.
   * @param thrown This is the exception which should be logged with the
   *   accompanying message.
   */
  public void log(Level level, String msg, Throwable thrown);

  /**
   * Gets the unique logging identifier associated with a given logger instance.
   * 
   * @return Returns the unique logging identifier for the logger instance.
   */
  public String getLoggerId();

  /**
   * Gets the current logging level associated with a given logger instance.
   * 
   * @return Returns the current logging level for the logger instance.
   */
  public Level getLogLevel();

  /**
   * Sets the logging level to be used for a given logger instance. If the
   * underlying logging service supports hierarchical logging this will also set
   * the log level for all loggers below the current instance in the log
   * hierarchy.
   * 
   * @param logLevel This is the log level to be used by the logger instance for
   *   all future log messages.
   */
  public void setLogLevel(Level logLevel);

}
