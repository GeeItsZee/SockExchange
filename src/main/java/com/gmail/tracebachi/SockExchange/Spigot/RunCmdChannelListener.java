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

import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessage;
import com.gmail.tracebachi.SockExchange.SockExchangeConstants.Channels;
import com.gmail.tracebachi.SockExchange.Utilities.BasicLogger;
import com.gmail.tracebachi.SockExchange.Utilities.Registerable;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;
import org.bukkit.command.ConsoleCommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class RunCmdChannelListener implements Consumer<ReceivedMessage>, Registerable
{
  private static final String LOG_FORMAT = "[RunCmd] Command sender from %s ran command /%s";

  private final SockExchangePlugin plugin;
  private final BasicLogger basicLogger;
  private final SockExchangeApi api;

  public RunCmdChannelListener(
    SockExchangePlugin plugin, BasicLogger basicLogger, SockExchangeApi api)
  {
    this.plugin = Preconditions.checkNotNull(plugin, "plugin");
    this.basicLogger = Preconditions.checkNotNull(basicLogger, "basicLogger");
    this.api = Preconditions.checkNotNull(api, "api");
  }

  @Override
  public void register()
  {
    api.getMessageNotifier().register(Channels.RUN_CMD, this);
  }

  @Override
  public void unregister()
  {
    api.getMessageNotifier().unregister(Channels.RUN_CMD, this);
  }

  @Override
  public void accept(ReceivedMessage message)
  {
    ByteArrayDataInput in = message.getDataInput();
    String sourceServerName = in.readUTF();
    int commandCount = in.readInt();
    List<String> commands = new ArrayList<>(commandCount);

    for (int i = 0; i < commandCount; i++)
    {
      commands.add(in.readUTF());
    }

    // Schedule synchronous running of the commands
    plugin.executeSync(() -> runCommands(sourceServerName, commands));
  }

  public void runCommands(String sourceServerName, List<String> commands)
  {
    ConsoleCommandSender consoleSender = plugin.getServer().getConsoleSender();

    for (String command : commands)
    {
      basicLogger.info(LOG_FORMAT, sourceServerName, command);

      plugin.getServer().dispatchCommand(consoleSender, command);
    }
  }
}
