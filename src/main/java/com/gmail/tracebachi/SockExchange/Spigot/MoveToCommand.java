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

import com.gmail.tracebachi.SockExchange.SockExchangeConstants.FormatNames;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import com.gmail.tracebachi.SockExchange.Utilities.Registerable;
import com.google.common.base.Preconditions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class MoveToCommand implements TabExecutor, Registerable
{
  private static final String COMMAND_NAME = "moveto";
  private static final String COMMAND_USAGE = "/moveto <server name>";
  private static final String PERM_COMMAND = "SockExchange.MoveTo";
  private static final String PERM_PRIVATE_SERVER_PREFIX = "SockExchange.MoveTo.";

  private final SockExchangePlugin plugin;
  private final MessageFormatMap formatMap;
  private final SockExchangeApi api;

  MoveToCommand(SockExchangePlugin plugin, MessageFormatMap formatMap, SockExchangeApi api)
  {
    this.plugin = Preconditions.checkNotNull(plugin, "plugin");
    this.formatMap = Preconditions.checkNotNull(formatMap, "formatMap");
    this.api = Preconditions.checkNotNull(api, "api");
  }

  @Override
  public void register()
  {
    plugin.getCommand(COMMAND_NAME).setExecutor(this);
  }

  @Override
  public void unregister()
  {
    plugin.getCommand(COMMAND_NAME).setExecutor(null);
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args)
  {
    if (!sender.hasPermission(PERM_COMMAND))
    {
      return Collections.emptyList();
    }

    String lastArg = args[args.length - 1].toLowerCase();
    Collection<SpigotServerInfo> serverInfos = api.getServerInfos();
    List<String> resultList = new ArrayList<>(serverInfos.size());

    for (SpigotServerInfo entry : serverInfos)
    {
      String serverName = entry.getServerName();

      if (!entry.isOnline() || !serverName.contains(lastArg))
      {
        continue;
      }

      if (entry.isPrivate() && !sender.hasPermission(PERM_PRIVATE_SERVER_PREFIX + serverName))
      {
        continue;
      }

      resultList.add(serverName);
    }

    return resultList;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
  {
    Collection<SpigotServerInfo> serverInfos = api.getServerInfos();

    if (args.length < 1)
    {
      String formattedServers = getFormattedServers(sender, serverInfos);
      sender.sendMessage(formatMap.format(FormatNames.USAGE, COMMAND_USAGE));
      sender.sendMessage(formatMap.format(FormatNames.CURRENT_SERVER, api.getServerName()));
      sender.sendMessage(formatMap.format(FormatNames.ONLINE_SERVER_LIST, formattedServers));
      return true;
    }

    if (!sender.hasPermission(PERM_COMMAND))
    {
      sender.sendMessage(formatMap.format(FormatNames.NO_PERM, PERM_COMMAND));
      return true;
    }

    if (!(sender instanceof Player))
    {
      sender.sendMessage(formatMap.format(FormatNames.PLAYER_ONLY_COMMAND, COMMAND_NAME));
      return true;
    }

    String destServerName = args[0];
    SpigotServerInfo serverInfo = api.getServerInfo(destServerName);

    if (serverInfo == null)
    {
      sender.sendMessage(formatMap.format(FormatNames.SERVER_NOT_FOUND, destServerName));
      return true;
    }

    if (serverInfo.isPrivate() && !sender.hasPermission(PERM_PRIVATE_SERVER_PREFIX + destServerName))
    {
      sender.sendMessage(formatMap.format(FormatNames.SERVER_NOT_FOUND, destServerName));
      return true;
    }

    if (!serverInfo.isOnline())
    {
      sender.sendMessage(formatMap.format(FormatNames.SERVER_NOT_ONLINE, destServerName));
      return true;
    }

    api.movePlayers(Collections.singleton(sender.getName()), serverInfo.getServerName());
    return true;
  }

  private String getFormattedServers(CommandSender sender, Collection<SpigotServerInfo> serverInfos)
  {
    String separator = ", ";
    StringBuilder builder = new StringBuilder();

    for (SpigotServerInfo serverInfo : serverInfos)
    {
      String serverName = serverInfo.getServerName();

      if (!serverInfo.isOnline())
      {
        continue;
      }

      if (serverInfo.isPrivate() && !sender.hasPermission(PERM_PRIVATE_SERVER_PREFIX + serverName))
      {
        continue;
      }

      builder.append(serverName);
      builder.append(separator);
    }

    if (builder.length() > 0)
    {
      builder.setLength(builder.length() - separator.length());
    }

    return builder.toString();
  }
}
