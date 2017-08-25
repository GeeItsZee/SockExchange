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

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

import java.net.ConnectException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class SockExchangeClient
{
  private final String hostname;
  private final int port;
  private final Bootstrap bootstrap;
  private final EventLoopGroup workerGroup = new NioEventLoopGroup(1);
  private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);

  private ConnectionState connectionState = ConnectionState.INITIAL;

  public SockExchangeClient(String hostname, int port, AbstractPacketHandler packetHandler)
  {
    Preconditions.checkArgument(hostname != null && !hostname.isEmpty(), "hostname");
    Preconditions.checkArgument(port > 0, "port");
    Preconditions.checkNotNull(packetHandler, "packetHandler");

    this.hostname = hostname;
    this.port = port;
    this.bootstrap = new Bootstrap()
      .group(workerGroup)
      .channel(NioSocketChannel.class)
      .handler(new SpigotPipelineInitializer(packetHandler));
  }

  public synchronized void start() throws Exception
  {
    if (connectionState != ConnectionState.INITIAL)
    {
      throw new IllegalStateException("SockExchangeClient has already been started");
    }

    connectionState = ConnectionState.NOT_CONNECTED;

    // Schedule task to try and connect if disconnected
    executorService.scheduleAtFixedRate(this::connect, 1, 1, TimeUnit.SECONDS);

    // Connect
    connectNow();
  }

  public synchronized void shutdown()
  {
    if (connectionState == ConnectionState.INITIAL)
    {
      throw new IllegalStateException("SockExchangeClient has not been started");
    }

    executorService.shutdown();
    workerGroup.shutdownGracefully();
  }

  private synchronized void connect()
  {
    if (connectionState == ConnectionState.NOT_CONNECTED)
    {
      // Mark state at connecting
      connectionState = ConnectionState.CONNECTING;

      // Use the bootstrap to start
      ChannelFuture future = bootstrap.connect(hostname, port);
      future.addListener(this::handleChannelConnectFuture);
      future.channel().closeFuture().addListener(this::handleChannelCloseFuture);
    }
  }

  private synchronized void connectNow() throws Exception
  {
    if (connectionState == ConnectionState.NOT_CONNECTED)
    {
      // Mark state at connecting
      connectionState = ConnectionState.CONNECTING;

      // Use the bootstrap to start
      ChannelFuture future = bootstrap.connect(hostname, port);
      future.addListener(this::handleChannelConnectFuture);
      future.channel().closeFuture().addListener(this::handleChannelCloseFuture);

      // Wait for the connection attempt to finish
      future.await();
    }
  }

  private synchronized void handleChannelConnectFuture(Future<? super Void> future)
  {
    if (future.isSuccess())
    {
      connectionState = ConnectionState.CONNECTED;
    }
    else
    {
      connectionState = ConnectionState.NOT_CONNECTED;

      Throwable cause = future.cause();
      printCauseToSystemErr(cause);
    }
  }

  private synchronized void handleChannelCloseFuture(Future<? super Void> future)
  {
    connectionState = ConnectionState.NOT_CONNECTED;
  }

  private void printCauseToSystemErr(Throwable cause)
  {
    if ((cause instanceof ConnectException))
    {
      String connectExceptionMessage = cause.getMessage();
      if (connectExceptionMessage.contains("Connection refused"))
      {
        System.err.println("[SockExchange] " + connectExceptionMessage);
      }
    }
    else
    {
      cause.printStackTrace(System.err);
    }
  }

  private enum ConnectionState
  {
    INITIAL,
    NOT_CONNECTED,
    CONNECTING,
    CONNECTED
  }
}
