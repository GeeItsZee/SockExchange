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
package com.gmail.tracebachi.SockExchange.Messages;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public enum ResponseStatus
{
  OK,
  NOT_CONNECTED,
  TIMED_OUT,
  SERVER_OFFLINE,
  SERVER_NOT_FOUND,
  PLAYER_NOT_FOUND;

  public boolean isOk()
  {
    return this == OK;
  }

  public static ResponseStatus fromOrdinal(int ordinal)
  {
    switch (ordinal)
    {
      case 0:
        return OK;
      case 1:
        return NOT_CONNECTED;
      case 2:
        return TIMED_OUT;
      case 3:
        return SERVER_OFFLINE;
      case 4:
        return SERVER_NOT_FOUND;
      case 5:
        return PLAYER_NOT_FOUND;
    }

    throw new IllegalArgumentException("Unknown mapping for ordinal");
  }
}
