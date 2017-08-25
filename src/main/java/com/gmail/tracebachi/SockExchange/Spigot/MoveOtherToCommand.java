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

import java.util.*;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class MoveOtherToCommand implements TabExecutor, Registerable
{
  private static final String COMMAND_NAME = "moveotherto";
  private static final String COMMAND_USAGE = "/moveotherto <server name> <player name>";
  private static final String PERM_COMMAND = "SockExchange.MoveOtherTo";
  private static final String NAME_FOR_ALL_ONLINE_PLAYERS = "@all";

  private final SockExchangePlugin plugin;
  private final MessageFormatMap formatMap;
  private final SockExchangeApi api;

  MoveOtherToCommand(SockExchangePlugin plugin, MessageFormatMap formatMap, SockExchangeApi api)
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

      if (entry.isOnline() && serverName.contains(lastArg))
      {
        resultList.add(serverName);
      }
    }

    return resultList;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
  {
    if (!sender.hasPermission(PERM_COMMAND))
    {
      sender.sendMessage(formatMap.format(FormatNames.NO_PERM, PERM_COMMAND));
      return true;
    }

    Collection<SpigotServerInfo> serverInfos = api.getServerInfos();

    if (args.length < 2)
    {
      String formattedOnlineServers = getFormattedOnlineServers(serverInfos);
      sender.sendMessage(formatMap.format(FormatNames.USAGE, COMMAND_USAGE));
      sender.sendMessage(formatMap.format(FormatNames.CURRENT_SERVER, api.getServerName()));
      sender.sendMessage(formatMap.format(FormatNames.ONLINE_SERVER_LIST, formattedOnlineServers));
      return true;
    }

    String destServerName = args[0];
    String playerToMoveName = args[1];
    SpigotServerInfo serverInfo = api.getServerInfo(destServerName);

    if (serverInfo == null)
    {
      sender.sendMessage(formatMap.format(FormatNames.SERVER_NOT_FOUND, destServerName));
      return true;
    }

    if (!serverInfo.isOnline())
    {
      sender.sendMessage(formatMap.format(FormatNames.SERVER_NOT_ONLINE, destServerName));
      return true;
    }

    Set<String> playerNamesSet = new HashSet<>();

    if (playerToMoveName.equalsIgnoreCase(NAME_FOR_ALL_ONLINE_PLAYERS))
    {
      for (Player player : plugin.getServer().getOnlinePlayers())
      {
        playerNamesSet.add(player.getName());
      }
    }
    else
    {
      playerNamesSet.add(playerToMoveName);
    }

    api.movePlayers(playerNamesSet, serverInfo.getServerName());
    return true;
  }

  private String getFormattedOnlineServers(Collection<SpigotServerInfo> serverInfos)
  {
    String separator = ", ";
    StringBuilder builder = new StringBuilder();

    for (SpigotServerInfo serverInfo : serverInfos)
    {
      if (serverInfo.isOnline())
      {
        builder.append(serverInfo.getServerName());
        builder.append(separator);
      }
    }

    if (builder.length() > 0)
    {
      builder.setLength(builder.length() - separator.length());
    }

    return builder.toString();
  }
}
