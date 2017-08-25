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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class CaseInsensitiveSet implements Set<String>
{
  private final Set<String> internalSet;

  public CaseInsensitiveSet(Set<String> internalSet)
  {
    Preconditions.checkNotNull(internalSet, "internalSet");

    if (!internalSet.isEmpty())
    {
      // Add all the strings to a temporary HashSet
      HashSet<String> tempSet = new HashSet<>(internalSet.size());
      tempSet.addAll(internalSet);

      // Clear the internal set
      internalSet.clear();

      // Add all the strings from the temporary HashSet into the internal set with a lower case
      for (String item : tempSet)
      {
        String lowerCasedStr = item.toLowerCase();
        internalSet.add(lowerCasedStr);
      }
    }

    this.internalSet = internalSet;
  }

  @Override
  public boolean removeIf(Predicate<? super String> filter)
  {
    return internalSet.removeIf(filter);
  }

  @Override
  public Stream<String> stream()
  {
    return internalSet.stream();
  }

  @Override
  public Stream<String> parallelStream()
  {
    return internalSet.parallelStream();
  }

  @Override
  public void forEach(Consumer<? super String> action)
  {
    internalSet.forEach(action);
  }

  @Override
  public int size()
  {
    return internalSet.size();
  }

  @Override
  public boolean isEmpty()
  {
    return internalSet.isEmpty();
  }

  @Override
  public boolean contains(Object str)
  {
    Preconditions.checkNotNull(str, "str");

    return internalSet.contains(((String) str).toLowerCase());
  }

  @Override
  public Iterator<String> iterator()
  {
    return internalSet.iterator();
  }

  @Override
  public Object[] toArray()
  {
    return internalSet.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a)
  {
    return internalSet.toArray(a);
  }

  @Override
  public boolean add(String str)
  {
    Preconditions.checkNotNull(str, "str");

    return internalSet.add(str.toLowerCase());
  }

  @Override
  public boolean remove(Object str)
  {
    Preconditions.checkNotNull(str, "str");

    return internalSet.remove(((String) str).toLowerCase());
  }

  @Override
  public boolean containsAll(Collection<?> c)
  {
    return internalSet.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends String> c)
  {
    for (String str : c)
    {
      internalSet.add(str.toLowerCase());
    }

    return true;
  }

  @Override
  public boolean retainAll(Collection<?> c)
  {
    return internalSet.retainAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c)
  {
    return internalSet.removeAll(c);
  }

  @Override
  public void clear()
  {
    internalSet.clear();
  }

  @Override
  public Spliterator<String> spliterator()
  {
    return internalSet.spliterator();
  }

  @Override
  public int hashCode()
  {
    return internalSet.hashCode();
  }

  @Override
  public boolean equals(Object obj)
  {
    return internalSet.equals(obj);
  }

  @Override
  public String toString()
  {
    return internalSet.toString();
  }

  public Class<?> getInternalSetClass()
  {
    return internalSet.getClass();
  }
}
