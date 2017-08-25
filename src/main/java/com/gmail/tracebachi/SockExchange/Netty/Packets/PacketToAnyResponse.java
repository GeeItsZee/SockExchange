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

import com.gmail.tracebachi.SockExchange.Messages.ResponseStatus;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class PacketToAnyResponse extends AbstractPacket
{
  private long consumerId;
  private ResponseStatus responseStatus;
  private byte[] messageBytes;

  public long getConsumerId()
  {
    return consumerId;
  }

  public void setConsumerId(long consumerId)
  {
    this.consumerId = consumerId;
  }

  public ResponseStatus getResponseStatus()
  {
    return responseStatus;
  }

  public void setResponseStatus(ResponseStatus responseStatus)
  {
    this.responseStatus = responseStatus;
  }

  public byte[] getMessageBytes()
  {
    return messageBytes;
  }

  public void setMessageBytes(byte[] messageBytes)
  {
    this.messageBytes = messageBytes;
  }

  @Override
  public void read(ByteBuf in)
  {
    // Read the consumer ID
    consumerId = in.readLong();

    // Read the ResponseStatus
    responseStatus = ResponseStatus.fromOrdinal(in.readByte());

    // Read the message bytes only if ResponseStatus was OK
    if (responseStatus.isOk())
    {
      int messageBytesCount = in.readInt();
      messageBytes = new byte[messageBytesCount];
      in.readBytes(messageBytes);
    }
    else
    {
      messageBytes = null;
    }
  }

  @Override
  public void write(ByteBuf out)
  {
    Preconditions.checkNotNull(responseStatus, "responseStatus");

    // Write the consumer ID
    out.writeLong(consumerId);

    // Write the ResponseStatus
    out.writeByte(responseStatus.ordinal());

    // Write the message bytes only if ResponseStatus is OK
    if (responseStatus.isOk())
    {
      Preconditions.checkNotNull(messageBytes, "messageBytes");

      out.writeInt(messageBytes.length);
      out.writeBytes(messageBytes);
    }
  }
}

