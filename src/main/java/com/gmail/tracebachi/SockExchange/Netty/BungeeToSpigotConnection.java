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
package com.gmail.tracebachi.SockExchange.Netty;

import com.gmail.tracebachi.SockExchange.Bungee.BungeeTieIn;
import com.gmail.tracebachi.SockExchange.ExpirableConsumer;
import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessage;
import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessageNotifier;
import com.gmail.tracebachi.SockExchange.Messages.ResponseMessage;
import com.gmail.tracebachi.SockExchange.Messages.ResponseStatus;
import com.gmail.tracebachi.SockExchange.Netty.Packets.*;
import com.gmail.tracebachi.SockExchange.Utilities.BasicLogger;
import com.gmail.tracebachi.SockExchange.Utilities.ExtraPreconditions;
import com.gmail.tracebachi.SockExchange.Utilities.LongIdCounterMap;
import com.google.common.base.Preconditions;
import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class BungeeToSpigotConnection extends AbstractPacketHandler
{
  private final String serverName;
  private final Executor executor;
  private final ReceivedMessageNotifier receivedMessageNotifier;
  private final LongIdCounterMap<ExpirableConsumer<ResponseMessage>> waitingForResponse;
  private final BasicLogger basicLogger;
  private final BungeeTieIn bungeeTieIn;

  public BungeeToSpigotConnection(
    String serverName, Executor executor, ReceivedMessageNotifier receivedMessageNotifier,
    LongIdCounterMap<ExpirableConsumer<ResponseMessage>> waitingForResponse,
    BasicLogger basicLogger, BungeeTieIn bungeeTieIn)
  {
    ExtraPreconditions.checkNotEmpty(serverName, "serverName");
    Preconditions.checkNotNull(executor, "executor");
    Preconditions.checkNotNull(receivedMessageNotifier, "receivedMessageNotifier");
    Preconditions.checkNotNull(waitingForResponse, "waitingForResponse");
    Preconditions.checkNotNull(basicLogger, "basicLogger");
    Preconditions.checkNotNull(bungeeTieIn, "bungeeTieIn");

    this.serverName = serverName;
    this.executor = executor;
    this.receivedMessageNotifier = receivedMessageNotifier;
    this.waitingForResponse = waitingForResponse;
    this.basicLogger = basicLogger;
    this.bungeeTieIn = bungeeTieIn;
  }

  public String getServerName()
  {
    return serverName;
  }

  @Override
  public void onChannelActive(Channel channel)
  {
    super.onChannelActive(channel);

    basicLogger.debug("[%s connection] Channel is now active", serverName);
  }

  @Override
  public void onChannelInactive()
  {
    basicLogger.debug("[%s connection] Channel is now inactive", serverName);

    super.onChannelInactive();
  }

  @Override
  public void handle(PacketToBungeeRequest packet)
  {
    Preconditions.checkNotNull(packet, "packet");
    Preconditions.checkState(channel != null, "Channel is not active");

    PacketToBungeeRequest.DestinationType destinationType = packet.getDestinationType();
    Long consumerId = packet.getConsumerId();

    if (destinationType == PacketToBungeeRequest.DestinationType.BUNGEE)
    {
      // Debug
      basicLogger.debug(
        "[%s connection] Received request. DestinationType: '%s'. ChannelName: '%s'. NumBytes: '%s'. ConsumerId: '%s'.",
        serverName, destinationType.name(), packet.getChannelName(),
        packet.getMessageBytes().length, consumerId);

      handleRequestForBungee(packet);
      return;
    }

    if (destinationType == PacketToBungeeRequest.DestinationType.SERVER_NAME)
    {
      String destServerName = packet.getServerOrPlayerName();

      // Debug
      basicLogger.debug(
        "[%s connection] Received request. DestinationType: '%s'. ServerName: '%s'. ChannelName: '%s'. NumBytes: '%s'. ConsumerId: '%s'.",
        serverName, destinationType.name(), destServerName, packet.getChannelName(),
        packet.getMessageBytes().length, consumerId);

      BungeeToSpigotConnection connection = bungeeTieIn.getConnection(destServerName);

      // If the connection does not exist, respond with SERVER_NOT_FOUND.
      if (connection == null)
      {
        if (packet.hasConsumer())
        {
          PacketToAnyResponse responsePacket = new PacketToAnyResponse();
          responsePacket.setConsumerId(packet.getConsumerId());
          responsePacket.setResponseStatus(ResponseStatus.SERVER_NOT_FOUND);
          responsePacket.setMessageBytes(null);

          sendPacket(responsePacket);
        }
        return;
      }

      handleRequestForSpigot(packet, connection);
      return;
    }

    if (destinationType == PacketToBungeeRequest.DestinationType.PLAYER_NAME)
    {
      String playerName = packet.getServerOrPlayerName();

      // Debug
      basicLogger.debug(
        "[%s connection] Received request. DestinationType: '%s'. PlayerName: '%s'. ChannelName: '%s'. NumBytes: '%s'. ConsumerId: '%s'.",
        serverName, destinationType.name(), playerName, packet.getChannelName(),
        packet.getMessageBytes().length, consumerId);

      String destServerName = bungeeTieIn.getServerNameForPlayer(playerName);

      // If the player does not exist, respond with PLAYER_NOT_FOUND.
      if (destServerName == null)
      {
        if (packet.hasConsumer())
        {
          PacketToAnyResponse responsePacket = new PacketToAnyResponse();
          responsePacket.setConsumerId(packet.getConsumerId());
          responsePacket.setResponseStatus(ResponseStatus.PLAYER_NOT_FOUND);
          responsePacket.setMessageBytes(null);

          sendPacket(responsePacket);
        }
        return;
      }

      BungeeToSpigotConnection connection = bungeeTieIn.getConnection(destServerName);

      // If the connection does not exist, respond with SERVER_NOT_FOUND.
      if (connection == null)
      {
        if (packet.hasConsumer())
        {
          PacketToAnyResponse responsePacket = new PacketToAnyResponse();
          responsePacket.setConsumerId(packet.getConsumerId());
          responsePacket.setResponseStatus(ResponseStatus.SERVER_NOT_FOUND);
          responsePacket.setMessageBytes(null);

          sendPacket(responsePacket);
        }
        return;
      }

      handleRequestForSpigot(packet, connection);
    }
  }

  @Override
  public void handle(PacketToAnyResponse packet)
  {
    Preconditions.checkNotNull(packet, "packet");
    Preconditions.checkState(channel != null, "Channel is not active");

    long consumerId = packet.getConsumerId();
    ResponseStatus responseStatus = packet.getResponseStatus();
    byte[] messageBytes = packet.getMessageBytes();
    ExpirableConsumer<ResponseMessage> responseConsumer = waitingForResponse.remove(consumerId);

    // Debug
    int bytesLen = messageBytes == null ? -1 : messageBytes.length;
    boolean foundConsumer = responseConsumer != null;
    basicLogger.debug(
      "[%s connection] Received response. ConsumerId: '%s'. ResponseStatus: '%s'. NumBytes: '%s'. FoundConsumer: '%s'.",
      serverName, consumerId, responseStatus, bytesLen, foundConsumer);

    if (foundConsumer)
    {
      executor.execute(() ->
      {
        ResponseMessage responseMessage = new ResponseMessage(responseStatus, messageBytes);
        responseConsumer.accept(responseMessage);
      });
    }
  }

  @Override
  public void handle(PacketToBungeeForward packet)
  {
    Preconditions.checkNotNull(packet, "packet");
    Preconditions.checkState(channel != null, "Channel is not active");

    String channelName = packet.getChannelName();
    byte[] messageBytes = packet.getMessageBytes();
    List<String> serverNames = packet.getServerNames();

    // Debug
    basicLogger.debug(
      "[%s connection] Received forward request. ChannelName: '%s'. NumBytes: '%s'. ServerNamesCount: '%s'.",
      serverName, channelName, messageBytes.length, serverNames.size());

    PacketToSpigotRequest packetToForward = new PacketToSpigotRequest();
    packetToForward.setChannelName(channelName);
    packetToForward.setMessageBytes(messageBytes);
    packetToForward.setConsumerId(null);

    if (serverNames.isEmpty())
    {
      // An empty set should send to all servers excluding source.
      for (BungeeToSpigotConnection connection : bungeeTieIn.getConnections())
      {
        if (connection != this)
        {
          connection.sendPacket(packetToForward);
        }
      }
    }
    else
    {
      // If a set is specified, forward to all matched servers.
      for (String serverName : serverNames)
      {
        BungeeToSpigotConnection connection = bungeeTieIn.getConnection(serverName);

        if (connection != null)
        {
          connection.sendPacket(packetToForward);
        }
      }
    }
  }

  public void sendToServer(
    String channelName, byte[] messageBytes, Consumer<ResponseMessage> consumer,
    long timeoutInMillis)
  {
    ExtraPreconditions.checkNotEmpty(channelName, "channelName");
    Preconditions.checkNotNull(messageBytes, "messageBytes");

    // If the connection does not have a channel, the server is offline.
    if (!hasChannel())
    {
      // If there is a consumer, respond with SERVER_OFFLINE.
      if (consumer != null)
      {
        executor.execute(() ->
        {
          ResponseMessage responseMessage = new ResponseMessage(ResponseStatus.SERVER_OFFLINE);
          consumer.accept(responseMessage);
        });
      }
      return;
    }

    PacketToSpigotRequest packetToSend = new PacketToSpigotRequest();
    packetToSend.setChannelName(channelName);
    packetToSend.setMessageBytes(messageBytes);

    // If the consumer is specified, save the consumer.
    if (consumer != null)
    {
      Preconditions.checkArgument(timeoutInMillis > 0, "timeoutInMillis must be > 0");

      ExpirableConsumer<ResponseMessage> responseConsumer = new ExpirableConsumer<>(consumer,
        System.currentTimeMillis() + timeoutInMillis);

      packetToSend.setConsumerId(waitingForResponse.put(responseConsumer));
    }

    // Send the packet
    sendPacket(packetToSend);
  }

  private void sendPacket(AbstractPacket packet)
  {
    if (channel != null)
    {
      channel.writeAndFlush(packet);
    }
  }

  private void handleRequestForBungee(PacketToBungeeRequest packet)
  {
    Consumer<byte[]> onResponseConsumer = null;
    String channelName = packet.getChannelName();
    byte[] messageBytes = packet.getMessageBytes();

    // If there is no consumer, there is no need to construct a response consumer.
    if (packet.hasConsumer())
    {
      long consumerId = packet.getConsumerId();

      // Create a response consumer that will send the response bytes to the source
      // server that sent the request
      onResponseConsumer = (bytes) ->
      {
        PacketToAnyResponse responsePacket = new PacketToAnyResponse();
        responsePacket.setConsumerId(consumerId);
        responsePacket.setResponseStatus(ResponseStatus.OK);
        responsePacket.setMessageBytes(bytes);

        sendPacket(responsePacket);
      };
    }

    // Notify channel listeners of the request
    ReceivedMessage receivedMessage = new ReceivedMessage(channelName, messageBytes,
      onResponseConsumer);
    receivedMessageNotifier.notify(channelName, receivedMessage);
  }

  private void handleRequestForSpigot(
    PacketToBungeeRequest packet, BungeeToSpigotConnection destConnection)
  {
    String channelName = packet.getChannelName();
    byte[] messageBytes = packet.getMessageBytes();

    // If the packet does not have a consumer ID, there is no need to create a consumer.
    if (!packet.hasConsumer())
    {
      destConnection.sendToServer(channelName, messageBytes, null, 0);
      return;
    }

    long consumerId = packet.getConsumerId();
    long timeoutInMillis = packet.getTimeoutInMillis();
    Consumer<ResponseMessage> messageConsumer = (responseMessage) ->
    {
      PacketToAnyResponse responsePacket = new PacketToAnyResponse();
      responsePacket.setConsumerId(consumerId);
      responsePacket.setResponseStatus(responseMessage.getResponseStatus());
      responsePacket.setMessageBytes(responseMessage.getMessageBytes());

      // Send the response packet from this connection
      sendPacket(responsePacket);
    };

    // ServerA makes the request and sends it to Bungee
    // Bungee makes a request to ServerB on behalf of ServerA
    // ServerB responds to Bungee
    // Bungee responds to ServerA
    destConnection.sendToServer(channelName, messageBytes, messageConsumer, timeoutInMillis);
  }
}
