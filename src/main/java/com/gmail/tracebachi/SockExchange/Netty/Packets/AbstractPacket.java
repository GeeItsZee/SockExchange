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

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public abstract class AbstractPacket
{
  /**
   * Reads the packet from a {@link ByteBuf}
   *
   * @param in ByteBuf to read from
   */
  public abstract void read(ByteBuf in);

  /**
   * Writes the packet to a {@link ByteBuf}
   *
   * @param out ByteBuf to write to
   */
  public abstract void write(ByteBuf out);

  /**
   * Helper method to write a VarShort
   *
   * @param out Output ByteBuf
   * @param value Value to write
   */
  private static void writeVarShort(ByteBuf out, short value)
  {
    // Long conversion required for AND-ing with 64 bits
    writeVarLong(out, (((long) 1 << 16) - 1) & value);
  }

  /**
   * Helper method to write a VarInt
   *
   * @param out Output ByteBuf
   * @param value Value to write
   */
  private static void writeVarInt(ByteBuf out, int value)
  {
    // Long conversion required for AND-ing with 64 bits
    writeVarLong(out, (((long) 1 << 32) - 1) & value);
  }

  /**
   * Helper method to write a VarLong
   *
   * @param out Output ByteBuf
   * @param value Value to write
   */
  private static void writeVarLong(ByteBuf out, long value)
  {
    do
    {
      byte toWrite = (byte) (value & 0x7F);

      value >>>= 7;

      if (value != 0)
      {
        toWrite |= 0x80;
      }

      out.writeByte(toWrite);
    }
    while (value != 0);
  }

  /**
   * Helper method to write a string
   *
   * @param out Output ByteBuf
   * @param str Value to write
   */
  protected static void writeString(ByteBuf out, String str)
  {
    if (str == null)
    {
      throw new NullPointerException("str");
    }

    int length = str.length();
    writeVarInt(out, length);

    byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
    out.writeBytes(strBytes);
  }

  /**
   * Helper method to read a VarShort
   *
   * @param in Input ByteBuf
   *
   * @return Short
   */
  private static short readVarShort(ByteBuf in)
  {
    return (short) readVarLong(in, 3);
  }

  /**
   * Helper method to read a VarInt
   *
   * @param in Input ByteBuf
   *
   * @return Integer
   */
  private static int readVarInt(ByteBuf in)
  {
    return (int) readVarLong(in, 5);
  }

  /**
   * Helper method to read a VarLong
   *
   * @param in Input ByteBuf
   *
   * @return Long
   */
  private static long readVarLong(ByteBuf in)
  {
    return readVarLong(in, 10);
  }

  /**
   * Helper method to read a string
   *
   * @param in Input ByteBuf
   *
   * @return String
   */
  protected static String readString(ByteBuf in)
  {
    int length = readVarInt(in);
    byte[] bytes = new byte[length];

    in.readBytes(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private static long readVarLong(ByteBuf out, int maxBytes)
  {
    long value = 0;

    for (int i = 0; i < maxBytes; i++)
    {
      byte readByte = out.readByte();

      // Long conversion required for > 32 bit shift
      value |= ((long) (readByte & 0x7F)) << (i * 7);

      if ((readByte & 0x80) != 0x80)
      {
        break;
      }
    }

    return value;
  }
}
