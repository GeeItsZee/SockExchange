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
package com.gmail.tracebachi.SockExchange.Spigot;

import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
class SockExchangeConfiguration
{
  private String hostName;
  private int port;
  private String serverName;
  private String registrationPassword;
  private MessageFormatMap messageFormatMap;
  private boolean debugMode;

  void read(ConfigurationSection configuration)
  {
    hostName = configuration.getString("SockExchangeClient.HostName");
    port = configuration.getInt("SockExchangeClient.Port", 20000);
    serverName = configuration.getString("SockExchangeClient.ServerName", "");
    registrationPassword = configuration.getString("SockExchangeClient.Password", "FreshSocks");
    debugMode = configuration.getBoolean("DebugMode", false);
    messageFormatMap = new MessageFormatMap();

    ConfigurationSection formats = configuration.getConfigurationSection("Formats");
    if (formats != null)
    {
      for (String formatKey : formats.getKeys(false))
      {
        String formatValue = formats.getString(formatKey, "");
        messageFormatMap.put(formatKey, ChatColor.translateAlternateColorCodes('&', formatValue));
      }
    }
  }

  String getHostName()
  {
    return hostName;
  }

  int getPort()
  {
    return port;
  }

  String getServerName()
  {
    return serverName;
  }

  String getRegistrationPassword()
  {
    return registrationPassword;
  }

  MessageFormatMap getMessageFormatMap()
  {
    return messageFormatMap;
  }

  boolean inDebugMode()
  {
    return debugMode;
  }
}
