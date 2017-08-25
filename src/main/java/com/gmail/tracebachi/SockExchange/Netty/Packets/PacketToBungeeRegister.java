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
import io.netty.buffer.ByteBuf;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class PacketToBungeeRegister extends AbstractPacket
{
  private String password;
  private String serverName;

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getServerName()
  {
    return serverName;
  }

  public void setServerName(String serverName)
  {
    this.serverName = serverName;
  }

  @Override
  public void read(ByteBuf in)
  {
    password = readString(in);
    serverName = readString(in);
  }

  @Override
  public void write(ByteBuf out)
  {
    ExtraPreconditions.checkNotEmpty(serverName, "serverName");

    writeString(out, password);
    writeString(out, serverName);
  }
}
