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

import com.gmail.tracebachi.SockExchange.Utilities.ExtraPreconditions;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class PacketToBungeeForward extends AbstractPacket
{
  private List<String> serverNames;
  private String channelName;
  private byte[] messageBytes;

  public List<String> getServerNames()
  {
    return serverNames;
  }

  public void setServerNames(List<String> serverNames)
  {
    this.serverNames = serverNames;
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

  @Override
  public void read(ByteBuf in)
  {
    // Read the server names
    int serverNameCount = in.readInt();
    serverNames = new ArrayList<>(serverNameCount);
    for (int i = 0; i < serverNameCount; i++)
    {
      serverNames.add(readString(in));
    }

    // Read the channel name
    channelName = readString(in);

    // Read the message bytes
    int messageBytesCount = in.readInt();
    messageBytes = new byte[messageBytesCount];
    in.readBytes(messageBytes);
  }

  @Override
  public void write(ByteBuf out)
  {
    Preconditions.checkNotNull(serverNames, "serverNames");
    ExtraPreconditions.checkNotEmpty(channelName, "channelName");
    Preconditions.checkNotNull(messageBytes, "messageBytes");

    // Write the server names
    out.writeInt(serverNames.size());
    for (String serverName : serverNames)
    {
      ExtraPreconditions.checkNotEmpty(serverName, "serverName");
      writeString(out, serverName);
    }

    // Write the channel name
    writeString(out, channelName);

    // Write the message bytes
    out.writeInt(messageBytes.length);
    out.writeBytes(messageBytes);
  }
}

