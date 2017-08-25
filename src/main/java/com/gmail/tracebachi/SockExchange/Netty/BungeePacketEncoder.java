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

import com.gmail.tracebachi.SockExchange.Netty.Packets.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static com.gmail.tracebachi.SockExchange.Netty.Packets.PacketIdMapping.isIdForPacket;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class BungeePacketEncoder extends MessageToByteEncoder<AbstractPacket>
{
  @Override
  protected void encode(ChannelHandlerContext ctx, AbstractPacket packet, ByteBuf out)
    throws Exception
  {
    byte packetId = PacketIdMapping.packetToId(packet.getClass());

    if (isIdForPacket(packetId, PacketToSpigotRegister.class) ||
      isIdForPacket(packetId, PacketToSpigotRequest.class) ||
      isIdForPacket(packetId, PacketToAnyResponse.class))
    {
      out.writeByte(packetId);
      packet.write(out);
    }
    else
    {
      // All acceptable packet (IDs) have been checked.
      System.err.println("[BungeePacketEncoder] Unexpected packetId: " + packetId);
      ctx.close();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception
  {
    // Print the exception and then close the channel
    System.err.println("[BungeePacketEncoder] Closing connection due to exception ...");
    throwable.printStackTrace(System.err);
    ctx.close();
  }
}
