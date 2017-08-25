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

import java.util.HashMap;
import java.util.Map;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class PacketIdMapping
{
  private static final Map<Byte, Class<? extends AbstractPacket>> idToPacket = new HashMap<>();
  private static final Map<Class<? extends AbstractPacket>, Byte> packetToId = new HashMap<>();

  static
  {
    byte packetId = 0;

    // Registration packets
    addIdAndPacketClassPair(++packetId, PacketToBungeeRegister.class);
    addIdAndPacketClassPair(++packetId, PacketToSpigotRegister.class);

    // Request and response packets
    addIdAndPacketClassPair(++packetId, PacketToBungeeRequest.class);
    addIdAndPacketClassPair(++packetId, PacketToSpigotRequest.class);
    addIdAndPacketClassPair(++packetId, PacketToAnyResponse.class);

    // Forward packet
    addIdAndPacketClassPair(++packetId, PacketToBungeeForward.class);
  }

  public static Class<? extends AbstractPacket> idToPacket(byte id)
  {
    Class<? extends AbstractPacket> clazz = idToPacket.get(id);
    if (clazz != null)
    {
      return clazz;
    }

    throw new IllegalArgumentException("Unknown packet ID: " + id);
  }

  public static byte packetToId(Class<? extends AbstractPacket> clazz)
  {
    Byte b = packetToId.get(clazz);
    if (b != null)
    {
      return b;
    }

    throw new IllegalArgumentException("Unknown packet class: " + clazz);
  }

  public static boolean isIdForPacket(byte id, Class<? extends AbstractPacket> clazz)
  {
    return packetToId(clazz) == id;
  }

  private static void addIdAndPacketClassPair(byte b, Class<? extends AbstractPacket> clazz)
  {
    Preconditions.checkArgument(!idToPacket.containsKey(b));
    Preconditions.checkArgument(!packetToId.containsKey(clazz));

    idToPacket.put(b, clazz);
    packetToId.put(clazz, b);
  }
}
