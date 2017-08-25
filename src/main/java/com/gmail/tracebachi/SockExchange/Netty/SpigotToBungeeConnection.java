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

import com.gmail.tracebachi.SockExchange.ExpirableConsumer;
import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessage;
import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessageNotifier;
import com.gmail.tracebachi.SockExchange.Messages.ResponseMessage;
import com.gmail.tracebachi.SockExchange.Messages.ResponseStatus;
import com.gmail.tracebachi.SockExchange.Netty.Packets.*;
import com.gmail.tracebachi.SockExchange.Netty.Packets.PacketToBungeeRequest.DestinationType;
import com.gmail.tracebachi.SockExchange.Utilities.BasicLogger;
import com.gmail.tracebachi.SockExchange.Utilities.ExtraPreconditions;
import com.gmail.tracebachi.SockExchange.Utilities.LongIdCounterMap;
import com.google.common.base.Preconditions;
import io.netty.channel.Channel;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class SpigotToBungeeConnection extends AbstractPacketHandler
{
  private final String serverName;
  private final String password;
  private final Executor executor;
  private final ReceivedMessageNotifier messageNotifier;
  private final LongIdCounterMap<ExpirableConsumer<ResponseMessage>> responseConsumerMap;
  private final BasicLogger basicLogger;
  private volatile boolean registered;

  public SpigotToBungeeConnection(
    String serverName, String password, Executor executor, ReceivedMessageNotifier messageNotifier,
    LongIdCounterMap<ExpirableConsumer<ResponseMessage>> responseConsumerMap,
    BasicLogger basicLogger)
  {
    ExtraPreconditions.checkNotEmpty(serverName, "serverName");
    ExtraPreconditions.checkNotEmpty(password, "password");
    Preconditions.checkNotNull(executor, "executor");
    Preconditions.checkNotNull(messageNotifier, "messageNotifier");
    Preconditions.checkNotNull(responseConsumerMap, "responseConsumerMap");
    Preconditions.checkNotNull(basicLogger, "basicLogger");

    this.serverName = serverName;
    this.password = password;
    this.executor = executor;
    this.messageNotifier = messageNotifier;
    this.responseConsumerMap = responseConsumerMap;
    this.basicLogger = basicLogger;
  }

  public String getServerName()
  {
    return serverName;
  }

  @Override
  public void onChannelActive(Channel channel)
  {
    super.onChannelActive(channel);

    PacketToBungeeRegister packet = new PacketToBungeeRegister();
    packet.setPassword(password);
    packet.setServerName(serverName);

    this.channel.writeAndFlush(packet);

    basicLogger.debug("Channel is now active.");
  }

  @Override
  public void onChannelInactive()
  {
    basicLogger.debug("Channel is now inactive.");

    registered = false;

    super.onChannelInactive();
  }

  @Override
  public void handle(PacketToSpigotRegister packet)
  {
    Preconditions.checkNotNull(packet, "packet");
    Preconditions.checkState(channel != null, "Channel is not active");

    if (registered)
    {
      channel.close();
      basicLogger.severe("Received a registration packet while registered");
    }
    else if (!packet.getResult().isSuccess())
    {
      channel.close();
      basicLogger.severe("Registration failed: %s", packet.getResult());
    }
    else
    {
      registered = true;
      basicLogger.debug("Channel is now registered.");
    }
  }

  @Override
  public void handle(PacketToSpigotRequest packet)
  {
    checkPacketAndRegistered(packet);

    Consumer<byte[]> onResponseConsumer = null;
    String channelName = packet.getChannelName();
    byte[] messageBytes = packet.getMessageBytes();
    Long consumerId = packet.getConsumerId();

    basicLogger.debug("Received request. ChannelName '%s'. NumBytes: '%s'. ConsumerId: '%s'.",
      channelName, messageBytes.length, consumerId);

    // If there is a consumer, construct a response consumer.
    if (packet.hasConsumer())
    {
      onResponseConsumer = (bytes) ->
      {
        PacketToAnyResponse responsePacket = new PacketToAnyResponse();
        responsePacket.setConsumerId(consumerId);
        responsePacket.setResponseStatus(ResponseStatus.OK);
        responsePacket.setMessageBytes(bytes);

        sendPacketIfRegistered(responsePacket);
      };
    }

    ReceivedMessage message = new ReceivedMessage(channelName, messageBytes, onResponseConsumer);
    messageNotifier.notify(channelName, message);
  }

  @Override
  public void handle(PacketToAnyResponse packet)
  {
    checkPacketAndRegistered(packet);

    long consumerId = packet.getConsumerId();
    ResponseStatus responseStatus = packet.getResponseStatus();
    byte[] messageBytes = packet.getMessageBytes();
    int numBytes = messageBytes != null ? messageBytes.length : -1;
    ExpirableConsumer<ResponseMessage> responseConsumer = responseConsumerMap.remove(consumerId);
    boolean hasConsumer = responseConsumer != null;

    basicLogger.debug(
      "Received response. ConsumerId: '%s'. ResponseStatus: '%s'. NumBytes: '%s'. HasConsumer: '%s'.",
      consumerId, responseStatus, numBytes, hasConsumer);

    // If there is a consumer, execute it with the response.
    if (hasConsumer)
    {
      executor.execute(() ->
      {
        ResponseMessage responseMessage = new ResponseMessage(responseStatus, messageBytes);
        responseConsumer.accept(responseMessage);
      });
    }
  }

  public void sendToBungee(
    String channelName, byte[] messageBytes, Consumer<ResponseMessage> consumer,
    long timeoutInMillis)
  {
    ExtraPreconditions.checkNotEmpty(channelName, "channelName");
    Preconditions.checkNotNull(messageBytes, "messageBytes");

    PacketToBungeeRequest packet = new PacketToBungeeRequest();
    packet.setDestinationType(DestinationType.BUNGEE);
    packet.setChannelName(channelName);
    packet.setMessageBytes(messageBytes);

    // Check if there is a consumer for a response
    if (consumer != null)
    {
      if (executeConsumerIfNotConnected(consumer))
      {
        return;
      }

      saveConsumerAndUpdatePacket(consumer, timeoutInMillis, packet);
    }

    sendPacketIfRegistered(packet);
  }

  public void sendToServer(
    String channelName, byte[] messageBytes, String destServerName,
    Consumer<ResponseMessage> consumer, long timeoutInMillis)
  {
    ExtraPreconditions.checkNotEmpty(channelName, "channelName");
    Preconditions.checkNotNull(messageBytes, "messageBytes");
    ExtraPreconditions.checkNotEmpty(destServerName, "destServerName");

    // Handle special case of sending a request to the current server
    if (destServerName.equalsIgnoreCase(serverName))
    {
      sendMessageToCurrentServer(consumer, channelName, messageBytes, timeoutInMillis);
      return;
    }

    PacketToBungeeRequest packet = new PacketToBungeeRequest();
    packet.setDestinationType(DestinationType.SERVER_NAME);
    packet.setServerOrPlayerName(destServerName);
    packet.setChannelName(channelName);
    packet.setMessageBytes(messageBytes);

    // Check if there is a consumer for a response
    if (consumer != null)
    {
      if (executeConsumerIfNotConnected(consumer))
      {
        return;
      }

      saveConsumerAndUpdatePacket(consumer, timeoutInMillis, packet);
    }

    sendPacketIfRegistered(packet);
  }

  public void sendToServerOfPlayer(
    String channelName, byte[] messageBytes, String playerName, Consumer<ResponseMessage> consumer,
    long timeoutInMillis)
  {
    ExtraPreconditions.checkNotEmpty(channelName, "channelName");
    Preconditions.checkNotNull(messageBytes, "messageBytes");
    ExtraPreconditions.checkNotEmpty(playerName, "playerName");

    PacketToBungeeRequest packet = new PacketToBungeeRequest();
    packet.setDestinationType(DestinationType.PLAYER_NAME);
    packet.setServerOrPlayerName(playerName);
    packet.setChannelName(channelName);
    packet.setMessageBytes(messageBytes);

    // Check if there is a consumer for a response
    if (consumer != null)
    {
      if (executeConsumerIfNotConnected(consumer))
      {
        return;
      }

      saveConsumerAndUpdatePacket(consumer, timeoutInMillis, packet);
    }

    sendPacketIfRegistered(packet);
  }

  public void sendToServers(
    String channelName, byte[] messageBytes, List<String> serverNameList)
  {
    ExtraPreconditions.checkNotEmpty(channelName, "channelName");
    Preconditions.checkNotNull(messageBytes, "messageBytes");

    // Null or empty lists are used to send to all connected servers.
    serverNameList = serverNameList == null ? Collections.emptyList() : serverNameList;

    ExtraPreconditions.checkElements(serverNameList, (str) -> str != null && !str.isEmpty(),
      "Null or empty string in serverNameList");

    PacketToBungeeForward packet = new PacketToBungeeForward();
    packet.setServerNames(serverNameList);
    packet.setChannelName(channelName);
    packet.setMessageBytes(messageBytes);

    sendPacketIfRegistered(packet);
  }

  private void checkPacketAndRegistered(AbstractPacket packet)
  {
    Preconditions.checkNotNull(packet, "packet");
    Preconditions.checkState(channel != null, "Channel is not active");

    if (registered)
    {
      return;
    }

    basicLogger.severe("Received a '%s' packet while not registered", packet.getClass().getName());
    channel.close();
  }

  private void sendPacketIfRegistered(AbstractPacket packet)
  {
    if (registered && channel != null)
    {
      channel.writeAndFlush(packet);
    }
  }

  private void sendMessageToCurrentServer(
    Consumer<ResponseMessage> consumer, String channelName, byte[] messageBytes,
    long timeoutInMillis)
  {
    Consumer<byte[]> onResponseConsumer = null;

    // If there is a consumer, save it and construct a response consumer.
    if (consumer != null)
    {
      long expiresAtMillis = System.currentTimeMillis() + timeoutInMillis;
      ExpirableConsumer<ResponseMessage> responseConsumer = new ExpirableConsumer<>(consumer,
        expiresAtMillis);
      long consumerId = responseConsumerMap.put(responseConsumer);

      onResponseConsumer = (bytes) ->
      {
        ExpirableConsumer<ResponseMessage> foundConsumer = responseConsumerMap.remove(consumerId);

        if (foundConsumer != null)
        {
          executor.execute(() ->
          {
            ResponseMessage responseMessage = new ResponseMessage(ResponseStatus.OK, bytes);
            foundConsumer.accept(responseMessage);
          });
        }
      };
    }

    ReceivedMessage message = new ReceivedMessage(channelName, messageBytes, onResponseConsumer);
    messageNotifier.notify(channelName, message);
  }

  private boolean executeConsumerIfNotConnected(Consumer<ResponseMessage> consumer)
  {
    if (registered && channel != null)
    {
      return false;
    }

    executor.execute(() ->
    {
      consumer.accept(new ResponseMessage(ResponseStatus.NOT_CONNECTED));
    });

    return true;
  }

  private void saveConsumerAndUpdatePacket(
    Consumer<ResponseMessage> consumer, long timeoutInMillis, PacketToBungeeRequest packet)
  {
    Preconditions.checkArgument(timeoutInMillis > 0, "timeoutInMillis must be > 0");

    ExpirableConsumer<ResponseMessage> responseConsumer = new ExpirableConsumer<>(
      consumer, System.currentTimeMillis() + timeoutInMillis);
    long assignedConsumerId = responseConsumerMap.put(responseConsumer);

    // Update the packet
    packet.setConsumerId(assignedConsumerId);
    packet.setTimeoutInMillis(timeoutInMillis);
  }
}
