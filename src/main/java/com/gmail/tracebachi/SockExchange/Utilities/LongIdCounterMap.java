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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class LongIdCounterMap<V>
{
  private final AtomicLong idCounter = new AtomicLong(0L);
  private final ConcurrentHashMap<Long, V> map = new ConcurrentHashMap<>();

  public long put(V value)
  {
    Preconditions.checkNotNull(value, "value");

    long id = idCounter.incrementAndGet();
    map.put(id, value);
    return id;
  }

  public V remove(long id)
  {
    return map.remove(id);
  }

  public void clear()
  {
    idCounter.set(0);
    map.clear();
  }

  public void removeIf(Predicate<Map.Entry<Long, V>> predicate)
  {
    Iterator<Map.Entry<Long, V>> iterator = map.entrySet().iterator();
    while (iterator.hasNext())
    {
      try
      {
        if (predicate.test(iterator.next()))
        {
          iterator.remove();
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
}
