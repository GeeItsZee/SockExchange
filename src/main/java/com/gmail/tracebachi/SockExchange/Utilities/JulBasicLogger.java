/*
 * SockExchange - Server and Client for BungeeCord and Spigot communication
 * Copyright (C) 2017 tracebachi@gmail.com (GeeItsZee)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.SockExchange.Utilities;

import com.google.common.base.Preconditions;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class JulBasicLogger implements BasicLogger
{
  private final Logger logger;
  private volatile boolean debugMode;

  public JulBasicLogger(Logger logger, boolean debugMode)
  {
    Preconditions.checkNotNull(logger, "logger");

    this.logger = logger;
    this.debugMode = debugMode;
  }

  public boolean inDebugMode()
  {
    return debugMode;
  }

  public void setDebugMode(boolean debugMode)
  {
    this.debugMode = debugMode;
  }

  @Override
  public void info(String format, Object... params)
  {
    logger.log(Level.INFO, formatWithParams(format, false, params));
  }

  @Override
  public void debug(String format, Object... params)
  {
    if (debugMode)
    {
      logger.log(Level.INFO, formatWithParams(format, true, params));
    }
  }

  @Override
  public void severe(String format, Object... params)
  {
    logger.log(Level.SEVERE, formatWithParams(format, false, params));
  }

  private static String formatWithParams(String format, boolean prefixWithDebug, Object... params)
  {
    if (format == null || params == null || params.length == 0)
    {
      return format;
    }

    StringBuilder builder;

    if (prefixWithDebug)
    {
      builder = new StringBuilder("[Debug] ");
    }
    else
    {
      builder = new StringBuilder(format.length() + 16 * params.length);
    }

    int formatLen = format.length();
    int strIdx = 0;
    int paramIdx = 0;

    while (paramIdx < params.length && strIdx < formatLen)
    {
      int indexOfNextParam = format.indexOf("%s", strIdx);
      if (indexOfNextParam >= 0)
      {
        // If there is a next param in the format, copy up to the index
        // of the param, append that substring, and then append the param.
        builder.append(format.substring(strIdx, indexOfNextParam));
        builder.append(params[paramIdx]);

        // Go to the next param in the array and skip the param format in
        // the format string.
        paramIdx++;
        strIdx = indexOfNextParam + 2;
      }
      else
      {
        // If there is no next param in the format, copy the rest of the format.
        builder.append(format.substring(strIdx, formatLen));
        strIdx = formatLen;
      }
    }

    if (strIdx < formatLen)
    {
      // If the number of params in the array was less than the number of "%s" in the string,
      // copy the rest of the string.
      builder.append(format.substring(strIdx, formatLen));
    }
    else if (paramIdx < params.length)
    {
      // If the number of "%s" in the string was less than the number of params in the array,
      // append the params at the end of the string.
      builder.append(" [");
      builder.append(params[paramIdx]);
      paramIdx++;

      while (paramIdx < params.length)
      {
        builder.append(", ");
        builder.append(params[paramIdx]);
        paramIdx++;
      }

      builder.append("]");
    }

    return builder.toString();
  }
}
