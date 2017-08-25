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
public class PacketToSpigotRegister extends AbstractPacket
{
  public enum Result
  {
    SUCCESS,
    INCORRECT_PASSWORD,
    ALREADY_REGISTERED,
    UNKNOWN_SERVER_NAME;

    public boolean isSuccess()
    {
      return this == SUCCESS;
    }

    public static Result fromOrdinal(int ordinal)
    {
      switch (ordinal)
      {
        case 0:
          return SUCCESS;
        case 1:
          return INCORRECT_PASSWORD;
        case 2:
          return ALREADY_REGISTERED;
        case 3:
          return UNKNOWN_SERVER_NAME;
      }

      throw new IllegalArgumentException("Unknown mapping for ordinal");
    }
  }

  private Result result;

  public Result getResult()
  {
    return result;
  }

  public void setResult(Result result)
  {
    this.result = result;
  }

  @Override
  public void read(ByteBuf in)
  {
    result = Result.fromOrdinal(in.readByte());
  }

  @Override
  public void write(ByteBuf out)
  {
    Preconditions.checkNotNull(result, "result");

    out.writeByte(result.ordinal());
  }
}
