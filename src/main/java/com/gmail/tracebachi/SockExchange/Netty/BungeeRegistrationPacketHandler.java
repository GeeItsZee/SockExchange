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
import com.gmail.tracebachi.SockExchange.Netty.Packets.PacketToBungeeRegister;
import com.gmail.tracebachi.SockExchange.Netty.Packets.PacketToSpigotRegister;
import com.google.common.base.Preconditions;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class BungeeRegistrationPacketHandler extends AbstractPacketHandler
{
  private final BungeeTieIn bungeeTieIn;

  public BungeeRegistrationPacketHandler(BungeeTieIn bungeeTieIn)
  {
    Preconditions.checkNotNull(bungeeTieIn, "bungeeTieIn");

    this.bungeeTieIn = bungeeTieIn;
  }

  @Override
  public void handle(PacketToBungeeRegister packet)
  {
    Preconditions.checkNotNull(packet, "packet");
    Preconditions.checkState(channel != null, "Inactive channel");

    PacketToSpigotRegister response = new PacketToSpigotRegister();
    String password = packet.getPassword();

    if (!bungeeTieIn.doesRegistrationPasswordMatch(password))
    {
      response.setResult(PacketToSpigotRegister.Result.INCORRECT_PASSWORD);
      channel.writeAndFlush(response).addListener((future) -> channel.close());
      return;
    }

    String serverName = packet.getServerName();
    BungeeToSpigotConnection connection = bungeeTieIn.getConnection(serverName);

    if (connection == null || !connection.getServerName().equals(serverName))
    {
      response.setResult(PacketToSpigotRegister.Result.UNKNOWN_SERVER_NAME);
      channel.writeAndFlush(response).addListener((future) -> channel.close());
      return;
    }

    if (connection.hasChannel())
    {
      response.setResult(PacketToSpigotRegister.Result.ALREADY_REGISTERED);
      channel.writeAndFlush(response).addListener((future) -> channel.close());
      return;
    }

    // Set up the BungeeToSpigotConnection for incoming packets
    connection.onChannelActive(channel);

    // Replace the decoder's packet handler with the BungeeToSpigotConnection
    channel.pipeline().get(BungeePacketDecoder.class).setPacketHandler(connection);

    response.setResult(PacketToSpigotRegister.Result.SUCCESS);
    channel.writeAndFlush(response);
  }
}
