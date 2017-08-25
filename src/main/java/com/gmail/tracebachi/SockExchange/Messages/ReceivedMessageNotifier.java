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
package com.gmail.tracebachi.SockExchange.Messages;

import com.gmail.tracebachi.SockExchange.Utilities.ExtraPreconditions;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class ReceivedMessageNotifier
{
  private final Executor executor;
  private final ConcurrentHashMap<String, CopyOnWriteArraySet<Consumer<ReceivedMessage>>> channelToConsumerSetMap;

  public ReceivedMessageNotifier(Executor executor)
  {
    Preconditions.checkNotNull(executor, "executor");

    this.executor = executor;
    this.channelToConsumerSetMap = new ConcurrentHashMap<>();
  }

  public void register(String channelName, Consumer<ReceivedMessage> consumer)
  {
    ExtraPreconditions.checkNotEmpty(channelName, "channelName");
    Preconditions.checkNotNull(consumer, "consumer");

    // Create a set containing the consumer (assuming the channel will not be found)
    CopyOnWriteArraySet<Consumer<ReceivedMessage>> newSet = new CopyOnWriteArraySet<>(
      Collections.singleton(consumer));

    CopyOnWriteArraySet<Consumer<ReceivedMessage>> existingSet = channelToConsumerSetMap
      .putIfAbsent(channelName, newSet);

    // If there was an existing mapping, existingSet will not be null (refer to putIfAbsent).
    if (existingSet != null)
    {
      existingSet.add(consumer);
    }
  }

  public void unregister(String channelName, Consumer<ReceivedMessage> consumer)
  {
    ExtraPreconditions.checkNotEmpty(channelName, "channelName");
    Preconditions.checkNotNull(consumer, "consumer");

    channelToConsumerSetMap.computeIfPresent(channelName, (entryKey, entrySet) ->
    {
      // Remove the consumer from the set
      entrySet.remove(consumer);

      // If the set is now empty, remove the mapping.
      return entrySet.size() == 0 ? null : entrySet;
    });
  }

  public void notify(String channelName, ReceivedMessage receivedMessage)
  {
    ExtraPreconditions.checkNotEmpty(channelName, "channelName");
    Preconditions.checkNotNull(receivedMessage, "receivedMessage");

    Set<Consumer<ReceivedMessage>> consumerSet = channelToConsumerSetMap.get(channelName);

    if (consumerSet == null)
    {
      return;
    }

    for (Consumer<ReceivedMessage> consumer : consumerSet)
    {
      executor.execute(() -> consumer.accept(receivedMessage));
    }
  }
}
