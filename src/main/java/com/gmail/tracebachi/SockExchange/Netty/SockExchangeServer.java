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
package com.gmail.tracebachi.SockExchange.Netty;

import com.gmail.tracebachi.SockExchange.Bungee.BungeeTieIn;
import com.google.common.base.Preconditions;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class SockExchangeServer
{
  private final int port;
  private final ServerBootstrap bootstrap;
  private final EventLoopGroup bossAndWorkerGroup;

  private boolean started = false;

  public SockExchangeServer(int port, int threads, BungeeTieIn tieIn)
  {
    Preconditions.checkArgument(port > 0, "port");
    Preconditions.checkNotNull(tieIn, "tieIn");

    this.port = port;
    this.bossAndWorkerGroup = new NioEventLoopGroup(Math.max(1, threads));
    this.bootstrap = new ServerBootstrap()
      .group(bossAndWorkerGroup)
      .channel(NioServerSocketChannel.class)
      .childHandler(new BungeePipelineInitializer(tieIn))
      .option(ChannelOption.SO_BACKLOG, 16);
  }

  public synchronized void start() throws Exception
  {
    Preconditions.checkState(!started, "SockExchangeServer has been started");

    // Try to bind to the port and await the result
    ChannelFuture bindFuture = bootstrap.bind(port);
    bindFuture.await();

    // If the bind failed, re-throw the cause of the failure
    if (!bindFuture.isSuccess())
    {
      throw new Exception(bindFuture.cause());
    }
    else
    {
      started = true;
    }
  }

  public synchronized void shutdown()
  {
    Preconditions.checkState(started, "SockExchangeServer has not been started");

    bossAndWorkerGroup.shutdownGracefully();
  }
}
