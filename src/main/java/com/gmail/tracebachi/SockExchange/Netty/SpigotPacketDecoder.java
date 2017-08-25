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

import com.gmail.tracebachi.SockExchange.Netty.Packets.PacketToAnyResponse;
import com.gmail.tracebachi.SockExchange.Netty.Packets.PacketToSpigotRegister;
import com.gmail.tracebachi.SockExchange.Netty.Packets.PacketToSpigotRequest;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import static com.gmail.tracebachi.SockExchange.Netty.Packets.PacketIdMapping.isIdForPacket;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class SpigotPacketDecoder extends MessageToMessageDecoder<ByteBuf>
{
  private final AbstractPacketHandler packetHandler;

  public SpigotPacketDecoder(AbstractPacketHandler packetHandler)
  {
    Preconditions.checkNotNull(packetHandler, "packetHandler");

    this.packetHandler = packetHandler;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception
  {
    byte packetId = in.readByte();

    if (isIdForPacket(packetId, PacketToSpigotRegister.class))
    {
      PacketToSpigotRegister packet = new PacketToSpigotRegister();
      packet.read(in);

      packetHandler.handle(packet);
    }
    else if (isIdForPacket(packetId, PacketToSpigotRequest.class))
    {
      PacketToSpigotRequest packet = new PacketToSpigotRequest();
      packet.read(in);

      packetHandler.handle(packet);
    }
    else if (isIdForPacket(packetId, PacketToAnyResponse.class))
    {
      PacketToAnyResponse packet = new PacketToAnyResponse();
      packet.read(in);

      packetHandler.handle(packet);
    }
    else
    {
      // All acceptable packet (IDs) have been checked.
      System.err.println("[SpigotPacketDecoder] Unexpected packetId: " + packetId);
      ctx.close();
    }

    if (in.isReadable())
    {
      // There should not be any leftover bytes.
      System.err.println("[SpigotPacketDecoder] Unexpected extra bytes. packetId: " + packetId);
      ctx.close();
    }
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception
  {
    packetHandler.onChannelActive(ctx.channel());
    super.channelActive(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception
  {
    packetHandler.onChannelInactive();
    super.channelInactive(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception
  {
    // Print the exception and then close the channel
    System.err.println("[SpigotPacketDecoder] Closing connection due to exception ...");
    throwable.printStackTrace(System.err);
    ctx.close();
  }
}
