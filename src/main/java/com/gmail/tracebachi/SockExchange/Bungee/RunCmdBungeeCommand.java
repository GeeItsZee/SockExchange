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

import com.gmail.tracebachi.SockExchange.SockExchangeConstants.FormatNames;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import com.gmail.tracebachi.SockExchange.Utilities.Registerable;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class RunCmdBungeeCommand extends Command implements Registerable
{
  private static final String COMMAND_NAME = "runcmdbungee";
  private static final String COMMAND_PERM = "SockExchange.Command.RunCmd";
  private static final String DEST_SPIGOT_SERVERS = "ALL";
  private static final String[] COMMAND_ALIASES = new String[] { "rcbungee" };

  private final SockExchangePlugin plugin;
  private final MessageFormatMap formatMap;
  private final SockExchangeApi api;

  RunCmdBungeeCommand(SockExchangePlugin plugin, MessageFormatMap formatMap, SockExchangeApi api)
  {
    super(COMMAND_NAME, COMMAND_PERM, COMMAND_ALIASES);

    Preconditions.checkNotNull(plugin, "plugin");
    Preconditions.checkNotNull(formatMap, "formatMap");

    this.plugin = plugin;
    this.formatMap = formatMap;
    this.api = api;
  }

  @Override
  public void register()
  {
    plugin.getProxy().getPluginManager().registerCommand(plugin, this);
  }

  @Override
  public void unregister()
  {
    plugin.getProxy().getPluginManager().unregisterCommand(this);
  }

  @Override
  public void execute(CommandSender sender, String[] args)
  {
    if (!sender.hasPermission(COMMAND_PERM))
    {
      sender.sendMessage(formatMap.format(FormatNames.NO_PERM, COMMAND_PERM));
      return;
    }

    if (args.length < 2)
    {
      sender.sendMessage(formatMap.format(FormatNames.USAGE, "/runcmd server[,server,..] command"));
      sender.sendMessage(formatMap.format(FormatNames.USAGE, "/runcmd ALL command"));
      return;
    }

    String[] argServers = args[0].split(",");
    String commandStr = joinArgsForCommand(args);

    if (doesArrayContain(argServers, DEST_SPIGOT_SERVERS))
    {
      api.sendCommandsToServers(Collections.singletonList(commandStr), Collections.emptyList());

      sender.sendMessage(formatMap.format(FormatNames.COMMAND_SENT, DEST_SPIGOT_SERVERS));
      return;
    }

    List<String> serverNames = new ArrayList<>(2);

    for (String dest : argServers)
    {
      SpigotServerInfo serverInfo = api.getServerInfo(dest);

      if (serverInfo == null)
      {
        sender.sendMessage(formatMap.format(FormatNames.SERVER_NOT_FOUND, dest));
      }
      else if (!serverInfo.isOnline())
      {
        sender.sendMessage(formatMap.format(FormatNames.SERVER_NOT_ONLINE, dest));
      }
      else
      {
        serverNames.add(serverInfo.getServerName());
      }
    }

    // Send the command to the servers that could be matched
    api.sendCommandsToServers(Collections.singletonList(commandStr), serverNames);

    for (String destServerName : serverNames)
    {
      sender.sendMessage(formatMap.format(FormatNames.COMMAND_SENT, destServerName));
    }
  }

  private String joinArgsForCommand(String[] args)
  {
    StringBuilder builder = new StringBuilder();

    builder.append(args[1]);
    for (int i = 2; i < args.length; i++)
    {
      builder.append(" ");
      builder.append(args[i]);
    }

    return builder.toString();
  }

  private boolean doesArrayContain(String[] array, String source)
  {
    for (String item : array)
    {
      if (item.equalsIgnoreCase(source))
      {
        return true;
      }
    }
    return false;
  }
}
