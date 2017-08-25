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

import com.gmail.tracebachi.SockExchange.Utilities.CaseInsensitiveSet;
import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;

import java.util.HashSet;
import java.util.Objects;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
class SockExchangeConfiguration
{
  private int port;
  private int connectionThreads;
  private String registrationPassword;
  private MessageFormatMap messageFormatMap;
  private boolean debugMode;
  private CaseInsensitiveSet privateServers = new CaseInsensitiveSet(new HashSet<>());

  void read(Configuration configuration)
  {
    port = configuration.getInt("SockExchangeServer.Port", 20000);
    connectionThreads = configuration.getInt("SockExchangeServer.Threads", 2);
    registrationPassword = configuration.getString("SockExchangeServer.Password", "FreshSocks");
    debugMode = configuration.getBoolean("DebugMode", false);
    messageFormatMap = new MessageFormatMap();

    privateServers.clear();
    privateServers.addAll(configuration.getStringList("PrivateServers"));

    Configuration formats = configuration.getSection("Formats");
    if (formats != null)
    {
      for (String formatKey : formats.getKeys())
      {
        String formatValue = formats.getString(formatKey, "");
        messageFormatMap.put(formatKey, ChatColor.translateAlternateColorCodes('&', formatValue));
      }
    }
  }

  int getPort()
  {
    return port;
  }

  int getConnectionThreads()
  {
    return connectionThreads;
  }

  boolean doesRegistrationPasswordMatch(String input)
  {
    return Objects.equals(registrationPassword, input);
  }

  MessageFormatMap getMessageFormatMap()
  {
    return messageFormatMap;
  }

  boolean inDebugMode()
  {
    return debugMode;
  }

  boolean isPrivateServer(String serverName)
  {
    return privateServers.contains(serverName);
  }
}
