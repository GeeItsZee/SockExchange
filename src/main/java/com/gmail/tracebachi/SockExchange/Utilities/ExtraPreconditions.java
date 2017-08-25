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

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class ExtraPreconditions
{
  private ExtraPreconditions()
  {

  }

  public static String checkNotEmpty(String reference, String referenceName)
  {
    if (referenceName == null)
    {
      throw new NullPointerException("referenceName");
    }
    else if (reference == null)
    {
      throw new NullPointerException(referenceName);
    }
    else if (reference.isEmpty())
    {
      throw new IllegalArgumentException(referenceName + " is empty");
    }
    else
    {
      return reference;
    }
  }

  public static <T extends Collection> T checkNotEmpty(T reference, String referenceName)
  {
    if (referenceName == null)
    {
      throw new NullPointerException("referenceName");
    }
    else if (reference == null)
    {
      throw new NullPointerException(referenceName);
    }
    else if (reference.isEmpty())
    {
      throw new IllegalArgumentException(referenceName + " is empty");
    }
    else
    {
      return reference;
    }
  }

  public static <T> void checkElements(
    Collection<T> collectionToCheck, Function<T, Boolean> elementOkIfTrueFunction,
    String errorMessage)
  {
    if (collectionToCheck == null)
    {
      throw new NullPointerException("collectionToCheck");
    }
    else if (elementOkIfTrueFunction == null)
    {
      throw new NullPointerException("elementOkIfTrueFunction");
    }
    else if (errorMessage == null)
    {
      throw new NullPointerException("errorMessage");
    }
    else
    {
      for (T item : collectionToCheck)
      {
        if (!elementOkIfTrueFunction.apply(item))
        {
          throw new IllegalArgumentException(errorMessage);
        }
      }
    }
  }

  public static <T> void checkElements(
    Collection<T> collectionToCheck, Function<T, String> elementTestFunction)
  {
    if (collectionToCheck == null)
    {
      throw new NullPointerException("collectionToCheck");
    }
    else if (elementTestFunction == null)
    {
      throw new NullPointerException("elementTestFunction");
    }
    else
    {
      for (T item : collectionToCheck)
      {
        String errorMessage = elementTestFunction.apply(item);

        if (errorMessage != null)
        {
          throw new IllegalArgumentException(errorMessage);
        }
      }
    }
  }

  public static <K, V> void checkEntries(
    Map<K, V> mapToCheck, Function<Map.Entry<K, V>, String> entryTestFunction)
  {
    if (mapToCheck == null)
    {
      throw new NullPointerException("mapToCheck");
    }
    else if (entryTestFunction == null)
    {
      throw new NullPointerException("entryTestFunction");
    }
    else
    {
      for (Map.Entry<K, V> entry : mapToCheck.entrySet())
      {
        String errorMessage = entryTestFunction.apply(entry);

        if (errorMessage != null)
        {
          throw new IllegalArgumentException(errorMessage);
        }
      }
    }
  }
}
