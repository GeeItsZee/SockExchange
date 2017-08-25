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
package com.gmail.tracebachi.SockExchange.Bungee;

import com.gmail.tracebachi.SockExchange.Netty.BungeeToSpigotConnection;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;

import java.util.Collection;
import java.util.List;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public interface BungeeTieIn
{
  boolean doesRegistrationPasswordMatch(String password);

  BungeeToSpigotConnection getConnection(String spigotServerName);

  Collection<BungeeToSpigotConnection> getConnections();

  SpigotServerInfo getServerInfo(String serverName);

  List<SpigotServerInfo> getServerInfos();

  String getServerNameForPlayer(String playerName);

  void sendChatMessagesToPlayer(String playerName, List<String> messages);

  void sendChatMessagesToConsole(List<String> messages);
}
