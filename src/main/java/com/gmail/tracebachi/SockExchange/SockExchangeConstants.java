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
package com.gmail.tracebachi.SockExchange;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class SockExchangeConstants
{
  public static class Channels
  {
    public static final String KEEP_ALIVE = "KeepAlive";
    public static final String RUN_CMD = "Command";
    public static final String MOVE_PLAYERS = "MovePlayers";
    public static final String CHAT_MESSAGES = "ChatMessages";
    public static final String PLAYER_UPDATE = "PlayerUpdate";
  }

  public static class FormatNames
  {
    public static final String NO_PERM = "NoPerm";
    public static final String USAGE = "Usage";
    public static final String PLAYER_ONLY_COMMAND = "PlayerOnlyCommand";
    public static final String COMMAND_SENT = "CommandSent";
    public static final String CURRENT_SERVER = "CurrentServer";
    public static final String ONLINE_SERVER_LIST = "OnlineServerList";
    public static final String SERVER_NOT_FOUND = "ServerNotFound";
    public static final String SERVER_NOT_ONLINE = "ServerNotOnline";
  }
}
