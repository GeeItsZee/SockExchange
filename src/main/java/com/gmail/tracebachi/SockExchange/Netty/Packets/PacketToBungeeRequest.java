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
package com.gmail.tracebachi.SockExchange.Netty.Packets;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class PacketToBungeeRequest extends AbstractPacket
{
  private DestinationType destinationType;
  private String serverOrPlayerName;
  private String channelName;
  private byte[] messageBytes;
  private Long consumerId;
  private long timeoutInMillis;

  public DestinationType getDestinationType()
  {
    return destinationType;
  }

  public void setDestinationType(DestinationType destinationType)
  {
    this.destinationType = destinationType;
  }

  public String getServerOrPlayerName()
  {
    return serverOrPlayerName;
  }

  public void setServerOrPlayerName(String serverOrPlayerName)
  {
    this.serverOrPlayerName = serverOrPlayerName;
  }

  public String getChannelName()
  {
    return channelName;
  }

  public void setChannelName(String channelName)
  {
    this.channelName = channelName;
  }

  public byte[] getMessageBytes()
  {
    return messageBytes;
  }

  public void setMessageBytes(byte[] messageBytes)
  {
    this.messageBytes = messageBytes;
  }

  public Long getConsumerId()
  {
    return consumerId;
  }

  public boolean hasConsumer()
  {
    return consumerId != null;
  }

  public void setConsumerId(Long consumerId)
  {
    this.consumerId = consumerId;
  }

  public long getTimeoutInMillis()
  {
    return timeoutInMillis;
  }

  public void setTimeoutInMillis(long timeoutInMillis)
  {
    this.timeoutInMillis = timeoutInMillis;
  }

  @Override
  public void read(ByteBuf in)
  {
    // Read the DestinationType
    destinationType = DestinationType.fromOrdinal(in.readByte());

    // Read the server or player name if the DestinationType is one of those
    if (destinationType == DestinationType.SERVER_NAME || destinationType == DestinationType.PLAYER_NAME)
    {
      serverOrPlayerName = readString(in);
    }

    // Read the channel name
    channelName = readString(in);

    // Read the message bytes
    int messageBytesCount = in.readInt();
    messageBytes = new byte[messageBytesCount];
    in.readBytes(messageBytes);

    // Read the consumer ID and timeout if there is a consumer for a response
    if (in.readBoolean())
    {
      consumerId = in.readLong();
      timeoutInMillis = in.readLong();
    }
    else
    {
      consumerId = null;
      timeoutInMillis = 0;
    }
  }

  @Override
  public void write(ByteBuf out)
  {
    Preconditions.checkNotNull(destinationType, "destinationType");
    Preconditions.checkNotNull(channelName, "channelName");
    Preconditions.checkNotNull(messageBytes, "messageBytes");

    // Write the DestinationType
    out.writeByte(destinationType.ordinal());

    // Write the server or player name if the DestinationType is one of those
    if (destinationType == DestinationType.SERVER_NAME || destinationType == DestinationType.PLAYER_NAME)
    {
      Preconditions.checkNotNull(serverOrPlayerName, "serverOrPlayerName");

      writeString(out, serverOrPlayerName);
    }

    // Write the channel name
    writeString(out, channelName);

    // Write the message bytes
    out.writeInt(messageBytes.length);
    out.writeBytes(messageBytes);

    // Write the consumer ID and timeout if there is a consumer for a response
    out.writeBoolean(consumerId != null);
    if (consumerId != null)
    {
      out.writeLong(consumerId);
      out.writeLong(timeoutInMillis);
    }
  }

  public enum DestinationType
  {
    BUNGEE,
    SERVER_NAME,
    PLAYER_NAME;

    public static DestinationType fromOrdinal(int ordinal)
    {
      switch (ordinal)
      {
        case 0:
          return BUNGEE;
        case 1:
          return SERVER_NAME;
        case 2:
          return PLAYER_NAME;
      }

      throw new IllegalArgumentException("Unknown mapping for ordinal");
    }
  }
}

