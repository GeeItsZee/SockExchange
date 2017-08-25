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

import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is not thread-safe, but if no formats are removed
 * after the initial {@link #put(String, String)}s, it won't
 * matter since reads will be thread-safe.
 *
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class MessageFormatMap
{
  private CaseInsensitiveMap<MessageFormat> formatMap = new CaseInsensitiveMap<>(
    new ConcurrentHashMap<>());

  /**
   * Adds or updates a new format
   *
   * @param formatName Name of the format to update
   * @param format String representation of the format like the one
   * that would be used in {@link MessageFormat}
   */
  public void put(String formatName, String format)
  {
    Preconditions.checkNotNull(formatName, "formatName");
    Preconditions.checkNotNull(format, "format");
    formatMap.put(formatName, new MessageFormat(format));
  }

  /**
   * Removes a format
   *
   * @param formatName Name of the format to update
   *
   * @return True if removed or false
   */
  public boolean remove(String formatName)
  {
    Preconditions.checkNotNull(formatName, "formatName");
    return formatMap.remove(formatName) != null;
  }

  /**
   * Clears the format map
   */
  public void clearFormats()
  {
    formatMap.clear();
  }

  /**
   * Uses a stored format with the provided arguments to return a formatted
   * string
   *
   * @param formatName Name of the format to use
   * @param arguments List of arguments
   *
   * @return Formatted string or string indicating that the format was not
   * found
   */
  public String format(String formatName, Object... arguments)
  {
    Preconditions.checkNotNull(formatName, "formatName");
    Preconditions.checkNotNull(arguments, "arguments");

    MessageFormat format = formatMap.get(formatName);
    if (format != null)
    {
      return format.format(arguments);
    }

    return "Format not found. FormatName: '" + formatName + "'.";
  }
}
