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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class CaseInsensitiveMap<T> implements Map<String, T>
{
  private final Map<String, T> internalMap;

  public CaseInsensitiveMap(Map<String, T> internalMap)
  {
    Preconditions.checkNotNull(internalMap, "internalMap");

    if (!internalMap.isEmpty())
    {
      // Put all the entries to a temporary HashMap
      HashMap<String, T> tempMap = new HashMap<>(internalMap.size());
      for (Entry<String, T> entry : internalMap.entrySet())
      {
        tempMap.put(entry.getKey(), entry.getValue());
      }

      // Clear the internal map
      internalMap.clear();

      // Put all the entries from the temporary HashMap into the internal map with a lower cased key
      for (Entry<String, T> entry : tempMap.entrySet())
      {
        String lowerCasedKey = entry.getKey().toLowerCase();
        internalMap.put(lowerCasedKey, entry.getValue());
      }
    }

    this.internalMap = internalMap;
  }

  @Override
  public int size()
  {
    return internalMap.size();
  }

  @Override
  public boolean isEmpty()
  {
    return internalMap.isEmpty();
  }

  @Override
  public boolean containsKey(Object key)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.containsKey(((String) key).toLowerCase());
  }

  @Override
  public boolean containsValue(Object value)
  {
    return internalMap.containsValue(value);
  }

  @Override
  public T get(Object key)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.get(((String) key).toLowerCase());
  }

  @Override
  public T put(String key, T value)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.put(key.toLowerCase(), value);
  }

  @Override
  public T remove(Object key)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.remove(((String) key).toLowerCase());
  }

  @Override
  public void putAll(Map<? extends String, ? extends T> m)
  {
    for (Entry<? extends String, ? extends T> entry : m.entrySet())
    {
      internalMap.put(entry.getKey().toLowerCase(), entry.getValue());
    }
  }

  @Override
  public void clear()
  {
    internalMap.clear();
  }

  @Override
  public Set<String> keySet()
  {
    return internalMap.keySet();
  }

  @Override
  public Collection<T> values()
  {
    return internalMap.values();
  }

  @Override
  public Set<Entry<String, T>> entrySet()
  {
    return internalMap.entrySet();
  }

  @Override
  public T getOrDefault(Object key, T defaultValue)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.getOrDefault(((String) key).toLowerCase(), defaultValue);
  }

  @Override
  public void forEach(BiConsumer<? super String, ? super T> action)
  {
    internalMap.forEach(action);
  }

  @Override
  public void replaceAll(BiFunction<? super String, ? super T, ? extends T> function)
  {
    internalMap.replaceAll(function);
  }

  @Override
  public T putIfAbsent(String key, T value)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.putIfAbsent(key.toLowerCase(), value);
  }

  @Override
  public boolean remove(Object key, Object value)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.remove(((String) key).toLowerCase(), value);
  }

  @Override
  public boolean replace(String key, T oldValue, T newValue)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.replace(key.toLowerCase(), oldValue, newValue);
  }

  @Override
  public T replace(String key, T value)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.replace(key.toLowerCase(), value);
  }

  @Override
  public T computeIfAbsent(String key, Function<? super String, ? extends T> mappingFunction)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.computeIfAbsent(key.toLowerCase(), mappingFunction);
  }

  @Override
  public T computeIfPresent(
    String key, BiFunction<? super String, ? super T, ? extends T> remappingFunction)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.computeIfPresent(key.toLowerCase(), remappingFunction);
  }

  @Override
  public T compute(
    String key, BiFunction<? super String, ? super T, ? extends T> remappingFunction)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.compute(key.toLowerCase(), remappingFunction);
  }

  @Override
  public T merge(
    String key, T value, BiFunction<? super T, ? super T, ? extends T> remappingFunction)
  {
    Preconditions.checkNotNull(key, "key");

    return internalMap.merge(key.toLowerCase(), value, remappingFunction);
  }

  @Override
  public int hashCode()
  {
    return internalMap.hashCode();
  }

  @Override
  public boolean equals(Object obj)
  {
    return internalMap.equals(obj);
  }

  @Override
  public String toString()
  {
    return internalMap.toString();
  }

  public Class<?> getInternalMapClass()
  {
    return internalMap.getClass();
  }
}
