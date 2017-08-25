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

import com.gmail.tracebachi.SockExchange.SockExchangeConstants.Channels;
import com.gmail.tracebachi.SockExchange.Utilities.Registerable;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class OnlinePlayerUpdateSender implements Registerable
{
  private final SockExchangePlugin plugin;
  private final SockExchangeApi api;
  private final long updatePeriodMillis;
  private ScheduledFuture<?> updateFuture;

  public OnlinePlayerUpdateSender(
    SockExchangePlugin plugin, SockExchangeApi api, long updatePeriodMillis)
  {
    Preconditions.checkNotNull(plugin, "plugin");
    Preconditions.checkNotNull(api, "api");
    Preconditions.checkArgument(updatePeriodMillis > 0, "updatePeriodMillis");

    this.plugin = plugin;
    this.api = api;
    this.updatePeriodMillis = updatePeriodMillis;
  }

  @Override
  public void register()
  {
    updateFuture = api.getScheduledExecutorService().scheduleAtFixedRate(
      this::sendPlayerUpdate, updatePeriodMillis, updatePeriodMillis,
      TimeUnit.MILLISECONDS);
  }

  @Override
  public void unregister()
  {
    if (updateFuture != null)
    {
      updateFuture.cancel(false);
      updateFuture = null;
    }
  }

  private void sendPlayerUpdate()
  {
    ByteArrayDataOutput out = ByteStreams.newDataOutput(1024);
    Collection<ProxiedPlayer> players = plugin.getProxy().getPlayers();

    out.writeInt(players.size());

    for (ProxiedPlayer proxiedPlayer : players)
    {
      out.writeUTF(proxiedPlayer.getName());
    }

    api.sendToServers(Channels.PLAYER_UPDATE, out.toByteArray());
  }
}
