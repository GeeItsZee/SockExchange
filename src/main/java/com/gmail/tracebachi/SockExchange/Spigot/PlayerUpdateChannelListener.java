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
package com.gmail.tracebachi.SockExchange.Spigot;

import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessage;
import com.gmail.tracebachi.SockExchange.SockExchangeConstants.Channels;
import com.gmail.tracebachi.SockExchange.Utilities.Registerable;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class PlayerUpdateChannelListener implements Registerable
{
  private final SockExchangeApi api;
  private final Consumer<ReceivedMessage> onChannelMessage;
  private volatile Set<String> onlinePlayerNames = Collections.emptySet();

  public PlayerUpdateChannelListener(SockExchangeApi api)
  {
    Preconditions.checkNotNull(api, "api");

    this.api = api;
    this.onChannelMessage = this::onServerInfoUpdateChannelMessage;
  }

  @Override
  public void register()
  {
    api.getMessageNotifier().register(Channels.PLAYER_UPDATE, onChannelMessage);
  }

  @Override
  public void unregister()
  {
    api.getMessageNotifier().unregister(Channels.PLAYER_UPDATE, onChannelMessage);
  }

  public Set<String> getOnlinePlayerNames()
  {
    return onlinePlayerNames;
  }

  private void onServerInfoUpdateChannelMessage(ReceivedMessage receivedMessage)
  {
    ByteArrayDataInput in = receivedMessage.getDataInput();
    int count = in.readInt();
    HashSet<String> set = new HashSet<>();

    for (int i = 0; i < count; i++)
    {
      String playerName = in.readUTF();
      set.add(playerName);
    }

    // Swap with new collection
    onlinePlayerNames = Collections.unmodifiableSet(set);
  }
}
