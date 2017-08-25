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

import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessageNotifier;
import com.gmail.tracebachi.SockExchange.Messages.ResponseMessage;
import com.gmail.tracebachi.SockExchange.Netty.BungeeToSpigotConnection;
import com.gmail.tracebachi.SockExchange.SockExchangeConstants.Channels;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.gmail.tracebachi.SockExchange.Utilities.ExtraPreconditions;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class SockExchangeApi
{
  /* Start: For singleton Api reference */

  private static SockExchangeApi instance;

  public static SockExchangeApi instance()
  {
    return instance;
  }

  protected static void setInstance(SockExchangeApi api)
  {
    instance = api;
  }

  /* End: For singleton Api reference */

  private final BungeeTieIn bungeeTieIn;
  private final ScheduledExecutorService scheduledExecutorService;
  private final ReceivedMessageNotifier messageNotifier;

  protected SockExchangeApi(
    BungeeTieIn bungeeTieIn, ScheduledExecutorService scheduledExecutorService,
    ReceivedMessageNotifier messageNotifier)
  {
    Preconditions.checkNotNull(bungeeTieIn, "bungeeTieIn");
    Preconditions.checkNotNull(scheduledExecutorService, "scheduledExecutorService");
    Preconditions.checkNotNull(messageNotifier, "messageNotifier");

    this.bungeeTieIn = bungeeTieIn;
    this.scheduledExecutorService = scheduledExecutorService;
    this.messageNotifier = messageNotifier;
  }

  /**
   * Get the SpigotServerInfo for a server using a name
   *
   * @param serverName Name of the server to lookup
   *
   * @return {@link SpigotServerInfo} or null if not found
   */
  public SpigotServerInfo getServerInfo(String serverName)
  {
    return bungeeTieIn.getServerInfo(serverName);
  }

  /**
   * @return List of {@link SpigotServerInfo} of all known servers
   */
  public List<SpigotServerInfo> getServerInfos()
  {
    return bungeeTieIn.getServerInfos();
  }

  /**
   * @return {@link ScheduledExecutorService} managed by SockExchange
   */
  public ScheduledExecutorService getScheduledExecutorService()
  {
    return scheduledExecutorService;
  }

  /**
   * @return {@link ReceivedMessageNotifier} used for registering and
   * un-registering listeners
   */
  public ReceivedMessageNotifier getMessageNotifier()
  {
    return messageNotifier;
  }

  /**
   * Sends bytes to one server (if online)
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: messageBytes
   *
   * @param channelName Name of channel to send bytes to
   * @param messageBytes Bytes to send
   * @param serverName Name of the server to send to
   */
  public void sendToServer(String channelName, byte[] messageBytes, String serverName)
  {
    sendToServer(channelName, messageBytes, serverName, null, 0);
  }

  /**
   * Sends bytes to one server (if online) and expects a response
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: messageBytes
   *
   * @param channelName Name of channel to send bytes to
   * @param messageBytes Bytes to send
   * @param serverName Name of the server to send to
   * @param consumer Consumer to run once there is a response (or a failure)
   * @param timeoutInMillis Milliseconds to wait for a response before returning a timeout response
   */
  public void sendToServer(
    String channelName, byte[] messageBytes, String serverName, Consumer<ResponseMessage> consumer,
    long timeoutInMillis)
  {
    BungeeToSpigotConnection connection = bungeeTieIn.getConnection(serverName);
    Preconditions.checkNotNull(connection, "Unknown serverName: %s", serverName);

    connection.sendToServer(channelName, messageBytes, consumer, timeoutInMillis);
  }

  /**
   * Sends bytes to all online servers
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: messageBytes
   *
   * @param channelName Name of channel to send bytes to
   * @param messageBytes Bytes to send
   */
  public void sendToServers(String channelName, byte[] messageBytes)
  {
    sendToServers(channelName, messageBytes, Collections.emptyList());
  }

  /**
   * Sends bytes to a list of servers (if online)
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: messageBytes, serverNames
   *
   * @param channelName Name of channel to send bytes to
   * @param messageBytes Bytes to send
   * @param serverNames List of server names to send bytes to
   */
  public void sendToServers(
    String channelName, byte[] messageBytes, List<String> serverNames)
  {
    ExtraPreconditions.checkNotEmpty(channelName, "channelName");
    Preconditions.checkNotNull(messageBytes, "messageBytes");

    if (serverNames == null || serverNames.isEmpty())
    {
      for (BungeeToSpigotConnection connection : bungeeTieIn.getConnections())
      {
        connection.sendToServer(channelName, messageBytes, null, 0);
      }
    }
    else
    {
      for (String serverName : serverNames)
      {
        if (serverName == null || serverName.isEmpty())
        {
          continue;
        }

        BungeeToSpigotConnection connection = bungeeTieIn.getConnection(serverName);

        if (connection == null)
        {
          continue;
        }

        connection.sendToServer(channelName, messageBytes, null, 0);
      }
    }
  }

  /**
   * Sends a list of commands to multiple servers (if online)
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: commands
   *
   * @param commands List of commands to run in order
   * @param serverNames Name of the server to send to
   */
  public void sendCommandsToServers(List<String> commands, List<String> serverNames)
  {
    ExtraPreconditions.checkNotEmpty(commands, "commands");
    ExtraPreconditions.checkElements(commands, (str) -> str != null && !str.isEmpty(),
      "Null or empty string in commands");

    byte[] messageBytes = getBytesForSendingCommands(commands);
    sendToServers(Channels.RUN_CMD, messageBytes, serverNames);
  }

  /**
   * Sends a list of messages (in order) to a player or console
   * <p>
   * If the player name is not null, empty, or console, the messages will be sent to the player
   * by the given name. Otherwise, the server name will be used to send messages to that server's
   * console. "Bungee" is a valid server name for the Bungee console.
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: messages
   *
   * @param messages List of messages to send
   * @param playerName Name of the player to send messages to
   * @param serverName Name of the server to send messages to
   */
  public void sendChatMessages(List<String> messages, String playerName, String serverName)
  {
    ExtraPreconditions.checkNotEmpty(messages, "messages");
    ExtraPreconditions.checkElements(messages, Objects::nonNull, "Null message in messages");

    // If a player name is provided and it's not the console, send it from Bungee
    if (playerName != null && !playerName.isEmpty() && !playerName.equalsIgnoreCase("console"))
    {
      bungeeTieIn.sendChatMessagesToPlayer(playerName, messages);
      return;
    }

    ExtraPreconditions.checkNotEmpty(serverName, "serverName");

    // Shortcut (since the message is for this server's console)
    if (serverName.equalsIgnoreCase("Bungee"))
    {
      bungeeTieIn.sendChatMessagesToConsole(messages);
      return;
    }

    byte[] messageBytes = getBytesForSendingChatMessages(messages);
    sendToServer(Channels.CHAT_MESSAGES, messageBytes, serverName);
  }

  private byte[] getBytesForSendingCommands(List<String> commands)
  {
    int commandsCount = commands.size();
    int approxSize = (commandsCount * 60 + 32) * 2;
    ByteArrayDataOutput out = ByteStreams.newDataOutput(approxSize);

    out.writeUTF("Bungee");

    out.writeInt(commandsCount);
    for (String command : commands)
    {
      out.writeUTF(command);
    }

    return out.toByteArray();
  }

  private byte[] getBytesForSendingChatMessages(List<String> messages)
  {
    int messagesCount = messages.size();
    int approxSize = (messagesCount * 60) * 2;
    ByteArrayDataOutput out = ByteStreams.newDataOutput(approxSize);

    out.writeInt(messagesCount);
    for (String message : messages)
    {
      out.writeUTF(message);
    }

    return out.toByteArray();
  }
}
