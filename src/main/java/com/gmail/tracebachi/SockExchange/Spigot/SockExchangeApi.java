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

import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessageNotifier;
import com.gmail.tracebachi.SockExchange.Messages.ResponseMessage;
import com.gmail.tracebachi.SockExchange.Netty.SpigotToBungeeConnection;
import com.gmail.tracebachi.SockExchange.SockExchangeConstants.Channels;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.gmail.tracebachi.SockExchange.Utilities.ExtraPreconditions;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.*;
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

  private final SpigotTieIn spigotTieIn;
  private final ScheduledExecutorService scheduledExecutorService;
  private final ReceivedMessageNotifier messageNotifier;
  private final SpigotToBungeeConnection connection;

  protected SockExchangeApi(
    SpigotTieIn spigotTieIn, ScheduledExecutorService scheduledExecutorService,
    ReceivedMessageNotifier messageNotifier, SpigotToBungeeConnection connection)
  {
    Preconditions.checkNotNull(spigotTieIn, "spigotTieIn");
    Preconditions.checkNotNull(scheduledExecutorService, "scheduledExecutorService");
    Preconditions.checkNotNull(messageNotifier, "messageNotifier");
    Preconditions.checkNotNull(connection, "connection");

    this.spigotTieIn = spigotTieIn;
    this.scheduledExecutorService = scheduledExecutorService;
    this.messageNotifier = messageNotifier;
    this.connection = connection;
  }

  /**
   * @return Name of the current server
   */
  public String getServerName()
  {
    return connection.getServerName();
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
    return spigotTieIn.getServerInfo(serverName);
  }

  /**
   * @return List of {@link SpigotServerInfo} of all known servers
   */
  public Collection<SpigotServerInfo> getServerInfos()
  {
    return spigotTieIn.getServerInfos();
  }

  /**
   * @return Set of player names which were last known to be online
   */
  public Set<String> getOnlinePlayerNames()
  {
    return spigotTieIn.getOnlinePlayerNames();
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
   * Sends bytes to Bungee (if connected)
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: messageBytes
   *
   * @param channelName Name of channel to send bytes to
   * @param messageBytes Bytes to send
   */
  public void sendToBungee(String channelName, byte[] messageBytes)
  {
    sendToBungee(channelName, messageBytes, null, 0);
  }

  /**
   * Sends bytes to Bungee (if connected) and expects a response
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: messageBytes
   *
   * @param channelName Name of channel to send bytes to
   * @param messageBytes Bytes to send
   * @param consumer Consumer to run once there is a response (or a failure)
   * @param timeoutInMillis Milliseconds to wait for a response before returning a timeout response
   */
  public void sendToBungee(
    String channelName, byte[] messageBytes, Consumer<ResponseMessage> consumer,
    long timeoutInMillis)
  {
    connection.sendToBungee(channelName, messageBytes, consumer, timeoutInMillis);
  }

  /**
   * Sends bytes to a server (if online)
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: messageBytes
   *
   * @param channelName Name of channel to send bytes to
   * @param messageBytes Bytes to send
   * @param destServerName Name of the server to send bytes to
   */
  public void sendToServer(String channelName, byte[] messageBytes, String destServerName)
  {
    sendToServer(channelName, messageBytes, destServerName, null, 0);
  }

  /**
   * Sends bytes to a server (if online) and expects a response
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: messageBytes
   *
   * @param channelName Name of channel to send bytes to
   * @param messageBytes Bytes to send
   * @param destServerName Name of the server to send bytes to
   * @param consumer Consumer to run once there is a response (or a failure)
   * @param timeoutInMillis Milliseconds to wait for a response before returning a timeout response
   */
  public void sendToServer(
    String channelName, byte[] messageBytes, String destServerName,
    Consumer<ResponseMessage> consumer, long timeoutInMillis)
  {
    connection.sendToServer(channelName, messageBytes, destServerName, consumer, timeoutInMillis);
  }

  /**
   * Sends bytes to a server (if online)
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: messageBytes
   *
   * @param channelName Name of channel to send bytes to
   * @param messageBytes Bytes to send
   * @param playerName Name of the player to find and then their server to send bytes to
   */
  public void sendToServerOfPlayer(String channelName, byte[] messageBytes, String playerName)
  {
    sendToServerOfPlayer(channelName, messageBytes, playerName, null, 0);
  }

  /**
   * Sends bytes to a server (if online) and expects a response
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: messageBytes
   *
   * @param channelName Name of channel to send bytes to
   * @param messageBytes Bytes to send
   * @param playerName Name of the player to find and then their server to send bytes to
   * @param consumer Consumer to run once there is a response (or a failure)
   * @param timeoutInMillis Milliseconds to wait for a response before returning a timeout response
   */
  public void sendToServerOfPlayer(
    String channelName, byte[] messageBytes, String playerName, Consumer<ResponseMessage> consumer,
    long timeoutInMillis)
  {
    spigotTieIn.isPlayerOnServer(playerName, (online) ->
    {
      if (online)
      {
        // Send the message to the current server
        connection.sendToServer(channelName, messageBytes, getServerName(), consumer,
          timeoutInMillis);
      }
      else
      {
        connection.sendToServerOfPlayer(channelName, messageBytes, playerName, consumer,
          timeoutInMillis);
      }
    });
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
    connection.sendToServers(channelName, messageBytes, serverNames);
  }

  /**
   * Sends players (if they are online) to the server (if the server is online)
   *
   * @param playerNames Set of player names to move
   * @param serverName Name of the server the player should be moved to
   */
  public void movePlayers(Set<String> playerNames, String serverName)
  {
    ExtraPreconditions.checkNotEmpty(playerNames, "playerNames");
    ExtraPreconditions.checkElements(playerNames, (str) -> str != null && !str.isEmpty(),
      "Null or empty name in playerNames");
    ExtraPreconditions.checkNotEmpty(serverName, "serverName");

    byte[] messageBytes = getBytesForMovingPlayers(serverName, playerNames);
    sendToBungee(Channels.MOVE_PLAYERS, messageBytes);
  }

  /**
   * Sends a list of commands to Bungee
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: commands
   *
   * @param commands List of commands to run in order
   */
  public void sendCommandsToBungee(List<String> commands)
  {
    ExtraPreconditions.checkNotEmpty(commands, "commands");
    ExtraPreconditions.checkElements(commands, (str) -> str != null && !str.isEmpty(),
      "Null or empty string in commands");

    byte[] messageBytes = getBytesForSendingCommands(getServerName(), commands);
    sendToBungee(Channels.RUN_CMD, messageBytes);
  }

  /**
   * Sends a list of commands to multiple servers (if they are online)
   * <p>
   * To avoid extra memory usage, the API assumes the following parameters
   * are not modified after this method is called: commands, serverNames
   *
   * @param commands List of commands to run in order
   * @param serverNames List of server names to send the command to
   */
  public void sendCommandsToServers(List<String> commands, List<String> serverNames)
  {
    ExtraPreconditions.checkNotEmpty(commands, "commands");
    ExtraPreconditions.checkElements(commands, (str) -> str != null && !str.isEmpty(),
      "Null or empty string in commands");

    byte[] messageBytes = getBytesForSendingCommands(getServerName(), commands);
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

    // If a player name is provided and it's not the console, send a message to Bungee
    if (playerName != null && !playerName.isEmpty() && !playerName.equalsIgnoreCase("console"))
    {
      byte[] messageBytes = getBytesForSendingChatMessages(messages, playerName, null);
      sendToBungee(Channels.CHAT_MESSAGES, messageBytes);
      return;
    }

    ExtraPreconditions.checkNotEmpty(serverName, "serverName");

    // Shortcut (since the message is for this server's console)
    if (getServerName().equalsIgnoreCase(serverName))
    {
      spigotTieIn.sendChatMessagesToConsole(messages);
      return;
    }

    byte[] messageBytes = getBytesForSendingChatMessages(messages, null, serverName);
    sendToBungee(Channels.CHAT_MESSAGES, messageBytes);
  }

  private static byte[] getBytesForMovingPlayers(String serverName, Set<String> playerNames)
  {
    int approxSize = ((playerNames.size() * 20) + serverName.length() + 32) * 2;
    ByteArrayDataOutput out = ByteStreams.newDataOutput(approxSize);

    out.writeUTF(serverName);

    out.writeInt(playerNames.size());
    for (String playerName : playerNames)
    {
      out.writeUTF(playerName);
    }

    return out.toByteArray();
  }

  private static byte[] getBytesForSendingCommands(String serverName, List<String> commands)
  {
    int commandsCount = commands.size();
    int approxSize = (commandsCount * 60 + 32) * 2;
    ByteArrayDataOutput out = ByteStreams.newDataOutput(approxSize);

    out.writeUTF(serverName);

    out.writeInt(commandsCount);
    for (String command : commands)
    {
      out.writeUTF(command);
    }

    return out.toByteArray();
  }

  private static byte[] getBytesForSendingChatMessages(
    List<String> messages, String playerName, String serverName)
  {
    int messagesCount = messages.size();
    int approxSize = (messagesCount * 60) * 2;
    ByteArrayDataOutput out = ByteStreams.newDataOutput(approxSize);

    out.writeInt(messages.size());
    for (String message : messages)
    {
      out.writeUTF(message);
    }

    if (playerName != null)
    {
      out.writeBoolean(true); // PlayerName
      out.writeUTF(playerName);
    }
    else if (serverName != null)
    {
      out.writeBoolean(false); // ServerName
      out.writeUTF(serverName);
    }
    else
    {
      throw new IllegalArgumentException("Both playerName and serverName are null");
    }

    return out.toByteArray();
  }
}
