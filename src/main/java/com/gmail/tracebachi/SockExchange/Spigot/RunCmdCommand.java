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
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class RunCmdCommand implements CommandExecutor, Registerable
{
  private static final String COMMAND_NAME = "runcmd";
  private static final String COMMAND_PERM = "SockExchange.RunCmd";
  private static final String DEST_BUNGEE = "BUNGEE";
  private static final String DEST_ALL_SPIGOT_SERVERS = "ALL";
  private static final String DEST_ALL_OTHER_SPIGOT_SERVERS = "ALL_OTHERS";

  private final SockExchangePlugin plugin;
  private final RunCmdChannelListener runCmdChannelListener;
  private final MessageFormatMap formatMap;
  private final SockExchangeApi api;

  RunCmdCommand(
    SockExchangePlugin plugin, RunCmdChannelListener runCmdChannelListener,
    MessageFormatMap formatMap, SockExchangeApi api)
  {
    Preconditions.checkNotNull(plugin, "plugin");
    Preconditions.checkNotNull(runCmdChannelListener, "runCmdChannelListener");
    Preconditions.checkNotNull(formatMap, "formatMap");
    Preconditions.checkNotNull(api, "api");

    this.plugin = plugin;
    this.runCmdChannelListener = runCmdChannelListener;
    this.formatMap = formatMap;
    this.api = api;
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
  public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
  {
    if (!sender.hasPermission(COMMAND_PERM))
    {
      sender.sendMessage(formatMap.format(FormatNames.NO_PERM, COMMAND_PERM));
      return true;
    }

    if (args.length < 2)
    {
      sender.sendMessage(formatMap.format(FormatNames.USAGE, "/runcmd server[,server,..] command"));
      sender.sendMessage(formatMap.format(FormatNames.USAGE, "/runcmd ALL command"));
      sender.sendMessage(formatMap.format(FormatNames.USAGE, "/runcmd ALL_OTHERS command"));
      sender.sendMessage(formatMap.format(FormatNames.USAGE, "/runcmd BUNGEE command"));
      return true;
    }

    String[] argServers = args[0].split(",");
    String commandStr = joinArgsForCommand(args);
    List<String> commands = Collections.singletonList(commandStr);

    if (doesArrayContain(argServers, DEST_BUNGEE))
    {
      api.sendCommandsToBungee(commands);

      sender.sendMessage(formatMap.format(FormatNames.COMMAND_SENT, DEST_BUNGEE));
      return true;
    }

    boolean toAll = doesArrayContain(argServers, DEST_ALL_SPIGOT_SERVERS);
    boolean toAllOthers = doesArrayContain(argServers, DEST_ALL_OTHER_SPIGOT_SERVERS);

    if (toAll || toAllOthers)
    {
      if (toAll)
      {
        String currentServerName = api.getServerName();
        runCmdChannelListener.runCommands(currentServerName, commands);
      }

      api.sendCommandsToServers(commands, Collections.emptyList());

      sender.sendMessage(formatMap.format(FormatNames.COMMAND_SENT, DEST_ALL_SPIGOT_SERVERS));
      return true;
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
    api.sendCommandsToServers(commands, serverNames);

    for (String destServerName : serverNames)
    {
      sender.sendMessage(formatMap.format(FormatNames.COMMAND_SENT, destServerName));
    }

    return true;
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
