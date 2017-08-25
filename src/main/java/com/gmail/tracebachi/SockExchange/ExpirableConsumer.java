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
package com.gmail.tracebachi.SockExchange;

import com.google.common.base.Preconditions;

import java.util.function.Consumer;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class ExpirableConsumer<T> implements Consumer<T>
{
  private final Consumer<T> innerConsumer;
  private final long expiresAtMillis;

  public ExpirableConsumer(Consumer<T> innerConsumer, long expiresAtMillis)
  {
    Preconditions.checkNotNull(innerConsumer, "innerConsumer");

    this.innerConsumer = innerConsumer;
    this.expiresAtMillis = expiresAtMillis;
  }

  public long getExpiresAtMillis()
  {
    return expiresAtMillis;
  }

  @Override
  public void accept(T responseMessage)
  {
    innerConsumer.accept(responseMessage);
  }
}
