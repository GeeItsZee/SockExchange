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
import com.google.common.base.Preconditions;
import io.netty.channel.Channel;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public abstract class AbstractPacketHandler
{
  protected volatile Channel channel;

  /**
   * @return True if the handler has a channel or false if not
   */
  public boolean hasChannel()
  {
    return channel != null;
  }

  /**
   * Informs the packet handler that the passed channel is now active
   */
  public void onChannelActive(Channel channel)
  {
    Preconditions.checkNotNull(channel, "channel");
    Preconditions.checkState(this.channel == null, "Channel is active");

    this.channel = channel;
  }

  /**
   * Informs the packet handler that the underlying channel is now inactive
   */
  public void onChannelInactive()
  {
    Preconditions.checkState(this.channel != null, "Channel is not active");

    this.channel = null;
  }

  /**
   * Handles registration start
   *
   * @param packet Packet to handle
   */
  public void handle(PacketToBungeeRegister packet)
  {
    throw new IllegalArgumentException(
      "Received an unsupported packet. Type: " + PacketToBungeeRegister.class.getName());
  }

  /**
   * Handles registration confirmation
   *
   * @param packet Packet to handle
   */
  public void handle(PacketToSpigotRegister packet)
  {
    throw new IllegalArgumentException(
      "Received an unsupported packet. Type: " + PacketToSpigotRegister.class.getName());
  }

  /**
   * Handles Bungee request packets
   *
   * @param packet Packet to handle
   */
  public void handle(PacketToBungeeRequest packet)
  {
    throw new IllegalArgumentException(
      "Received an unsupported packet. Type: " + PacketToBungeeRequest.class.getName());
  }

  /**
   * Handles Spigot request packets
   *
   * @param packet Packet to handle
   */
  public void handle(PacketToSpigotRequest packet)
  {
    throw new IllegalArgumentException(
      "Received an unsupported packet. Type: " + PacketToSpigotRequest.class.getName());
  }

  /**
   * Handles forwarding packets
   *
   * @param packet Packet to handle
   */
  public void handle(PacketToBungeeForward packet)
  {
    throw new IllegalArgumentException(
      "Received an unsupported packet. Type: " + PacketToBungeeForward.class.getName());
  }

  /**
   * Handles response packets
   *
   * @param packet Packet to handle
   */
  public void handle(PacketToAnyResponse packet)
  {
    throw new IllegalArgumentException(
      "Received an unsupported packet. Type: " + PacketToAnyResponse.class.getName());
  }
}
