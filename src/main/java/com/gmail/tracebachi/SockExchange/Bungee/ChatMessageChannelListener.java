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
package com.gmail.tracebachi.SockExchange.Bungee;

import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessage;
import com.gmail.tracebachi.SockExchange.SockExchangeConstants.Channels;
import com.gmail.tracebachi.SockExchange.Utilities.Registerable;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class ChatMessageChannelListener implements Consumer<ReceivedMessage>, Registerable
{
  private final SockExchangeApi api;

  ChatMessageChannelListener(SockExchangeApi api)
  {
    Preconditions.checkNotNull(api, "api");

    this.api = api;
  }

  @Override
  public void register()
  {
    api.getMessageNotifier().register(Channels.CHAT_MESSAGES, this);
  }

  @Override
  public void unregister()
  {
    api.getMessageNotifier().unregister(Channels.CHAT_MESSAGES, this);
  }

  @Override
  public void accept(ReceivedMessage message)
  {
    ByteArrayDataInput in = message.getDataInput();

    // Read all the chat messages
    int messagesCount = in.readInt();
    List<String> chatMessages = new ArrayList<>();
    for (int i = 0; i < messagesCount; i++)
    {
      chatMessages.add(in.readUTF());
    }

    String playerName = null;
    String serverName = null;
    boolean forPlayer = in.readBoolean();

    if (forPlayer)
    {
      playerName = in.readUTF();
    }
    else
    {
      serverName = in.readUTF();
    }

    api.sendChatMessages(chatMessages, playerName, serverName);
  }
}
