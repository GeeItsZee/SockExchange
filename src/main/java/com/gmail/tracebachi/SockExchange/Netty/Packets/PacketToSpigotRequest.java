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
public class PacketToSpigotRequest extends AbstractPacket
{
  private String channelName;
  private byte[] messageBytes;
  private Long consumerId;

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

  @Override
  public void read(ByteBuf in)
  {
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
    }
    else
    {
      consumerId = null;
    }
  }

  @Override
  public void write(ByteBuf out)
  {
    Preconditions.checkNotNull(channelName, "channelName");
    Preconditions.checkNotNull(messageBytes, "messageBytes");

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
    }
  }
}

