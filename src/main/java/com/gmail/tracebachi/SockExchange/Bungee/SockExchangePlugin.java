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

import com.gmail.tracebachi.SockExchange.ExpirableConsumer;
import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessageNotifier;
import com.gmail.tracebachi.SockExchange.Messages.ResponseMessage;
import com.gmail.tracebachi.SockExchange.Messages.ResponseStatus;
import com.gmail.tracebachi.SockExchange.Netty.BungeeToSpigotConnection;
import com.gmail.tracebachi.SockExchange.Netty.SockExchangeServer;
import com.gmail.tracebachi.SockExchange.Scheduler.AwaitableExecutor;
import com.gmail.tracebachi.SockExchange.Scheduler.ScheduledExecutorServiceWrapper;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.gmail.tracebachi.SockExchange.Utilities.*;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class SockExchangePlugin extends Plugin implements BungeeTieIn
{
  private final SockExchangeConfiguration configuration = new SockExchangeConfiguration();

  private BasicLogger basicLogger;
  private ScheduledThreadPoolExecutor threadPoolExecutor;
  private AwaitableExecutor awaitableExecutor;
  private ReceivedMessageNotifier messageNotifier;
  private LongIdCounterMap<ExpirableConsumer<ResponseMessage>> responseConsumerMap;
  private ScheduledFuture<?> consumerTimeoutCleanupFuture;
  private CaseInsensitiveMap<BungeeToSpigotConnection> spigotConnectionMap;
  private SockExchangeServer sockExchangeServer;

  private OnlinePlayerUpdateSender onlinePlayerUpdateSender;
  private BungeeKeepAliveSender bungeeKeepAliveSender;
  private RunCmdBungeeCommand runCmdBungeeCommand;
  private MovePlayersChannelListener movePlayersChannelListener;
  private RunCmdChannelListener runCmdChannelListener;
  private ChatMessageChannelListener chatMessageChannelListener;

  @Override
  public void onEnable()
  {
    if (!reloadConfiguration())
    {
      return;
    }

    boolean debugMode = configuration.inDebugMode();
    int port = configuration.getPort();
    int connectionThreads = configuration.getConnectionThreads();
    MessageFormatMap messageFormatMap = configuration.getMessageFormatMap();

    // Create the logger based on Java.Util.Logging
    basicLogger = new JulBasicLogger(getLogger(), debugMode);

    // Create the shared thread pool executor
    buildThreadPoolExecutor();
    ScheduledExecutorServiceWrapper wrappedThreadPool =
      new ScheduledExecutorServiceWrapper(threadPoolExecutor);

    // Create the AwaitableExecutor
    awaitableExecutor = new AwaitableExecutor(wrappedThreadPool);

    // Create the message notifier which will run consumers on SockExchange messages
    messageNotifier = new ReceivedMessageNotifier(awaitableExecutor);

    // Create the map that manages consumers for responses to sent message
    responseConsumerMap = new LongIdCounterMap<>();

    // Schedule a task to clean up the responseConsumerMap (handling timeouts)
    consumerTimeoutCleanupFuture = threadPoolExecutor.scheduleWithFixedDelay(
      this::checkForConsumerTimeouts, 5, 5, TimeUnit.SECONDS);

    // Create the map of known spigot servers that can connect to Bungee
    spigotConnectionMap = new CaseInsensitiveMap<>(new ConcurrentHashMap<>());
    for (String serverName : getProxy().getServers().keySet())
    {
      BungeeToSpigotConnection connection = new BungeeToSpigotConnection(
        serverName, awaitableExecutor, messageNotifier, responseConsumerMap, basicLogger, this);

      spigotConnectionMap.put(serverName, connection);
    }

    // Create the API
    SockExchangeApi api = new SockExchangeApi(this, wrappedThreadPool, messageNotifier);
    SockExchangeApi.setInstance(api);

    onlinePlayerUpdateSender = new OnlinePlayerUpdateSender(this, api, 5000);
    onlinePlayerUpdateSender.register();

    bungeeKeepAliveSender = new BungeeKeepAliveSender(this, api, 2000);
    bungeeKeepAliveSender.register();

    runCmdBungeeCommand = new RunCmdBungeeCommand(this, messageFormatMap, api);
    runCmdBungeeCommand.register();

    movePlayersChannelListener = new MovePlayersChannelListener(this, api);
    movePlayersChannelListener.register();

    runCmdChannelListener = new RunCmdChannelListener(this, basicLogger, api);
    runCmdChannelListener.register();

    chatMessageChannelListener = new ChatMessageChannelListener(api);
    chatMessageChannelListener.register();

    try
    {
      sockExchangeServer = new SockExchangeServer(port, connectionThreads, this);
      sockExchangeServer.start();
    }
    catch (Exception e)
    {
      getLogger().severe("============================================================");
      getLogger().severe("The SockExchange server could not be started. Refer to the stacktrace below.");
      e.printStackTrace();
      getLogger().severe("============================================================");
    }
  }

  @Override
  public void onDisable()
  {
    // Shut down the AwaitableExecutor first so tasks are not running
    // when shutting down everything else
    if (awaitableExecutor != null)
    {
      shutdownAwaitableExecutor();
      awaitableExecutor = null;
    }

    if (sockExchangeServer != null)
    {
      sockExchangeServer.shutdown();
      sockExchangeServer = null;
    }

    if (chatMessageChannelListener != null)
    {
      chatMessageChannelListener.unregister();
      chatMessageChannelListener = null;
    }

    if (runCmdChannelListener != null)
    {
      runCmdChannelListener.unregister();
      runCmdChannelListener = null;
    }

    if (movePlayersChannelListener != null)
    {
      movePlayersChannelListener.unregister();
      movePlayersChannelListener = null;
    }

    if (runCmdBungeeCommand != null)
    {
      runCmdBungeeCommand.unregister();
      runCmdBungeeCommand = null;
    }

    if (bungeeKeepAliveSender != null)
    {
      bungeeKeepAliveSender.unregister();
      bungeeKeepAliveSender = null;
    }

    if (onlinePlayerUpdateSender != null)
    {
      onlinePlayerUpdateSender.unregister();
      onlinePlayerUpdateSender = null;
    }

    SockExchangeApi.setInstance(null);

    if (spigotConnectionMap != null)
    {
      spigotConnectionMap.clear();
      spigotConnectionMap = null;
    }

    if (consumerTimeoutCleanupFuture != null)
    {
      consumerTimeoutCleanupFuture.cancel(false);
      consumerTimeoutCleanupFuture = null;
    }

    if (responseConsumerMap != null)
    {
      responseConsumerMap.clear();
      responseConsumerMap = null;
    }

    if (threadPoolExecutor != null)
    {
      shutdownThreadPoolExecutor();
      threadPoolExecutor = null;
    }

    messageNotifier = null;
    basicLogger = null;
  }

  @Override
  public boolean doesRegistrationPasswordMatch(String password)
  {
    return configuration.doesRegistrationPasswordMatch(password);
  }

  @Override
  public BungeeToSpigotConnection getConnection(String serverName)
  {
    ExtraPreconditions.checkNotEmpty(serverName, "serverName");

    return spigotConnectionMap.get(serverName);
  }

  @Override
  public Collection<BungeeToSpigotConnection> getConnections()
  {
    return Collections.unmodifiableCollection(spigotConnectionMap.values());
  }

  @Override
  public SpigotServerInfo getServerInfo(String serverName)
  {
    Preconditions.checkNotNull(serverName, "serverName");

    BungeeToSpigotConnection connection = spigotConnectionMap.get(serverName);

    if (connection == null)
    {
      return null;
    }

    boolean isPrivate = configuration.isPrivateServer(connection.getServerName());
    return new SpigotServerInfo(connection.getServerName(), connection.hasChannel(), isPrivate);
  }

  @Override
  public List<SpigotServerInfo> getServerInfos()
  {
    Collection<BungeeToSpigotConnection> connections = spigotConnectionMap.values();
    List<SpigotServerInfo> result = new ArrayList<>(connections.size());

    for (BungeeToSpigotConnection connection : connections)
    {
      boolean isPrivate = configuration.isPrivateServer(connection.getServerName());
      SpigotServerInfo serverInfo = new SpigotServerInfo(connection.getServerName(),
        connection.hasChannel(), isPrivate);

      result.add(serverInfo);
    }

    return Collections.unmodifiableList(result);
  }

  @Override
  public String getServerNameForPlayer(String playerName)
  {
    ProxiedPlayer player = getProxy().getPlayer(playerName);

    if (player == null)
    {
      return null;
    }

    Server server = player.getServer();

    if (server == null)
    {
      return null;
    }

    return server.getInfo().getName();
  }

  @Override
  public void sendChatMessagesToPlayer(String playerName, List<String> messages)
  {
    Preconditions.checkNotNull(playerName, "receiverName");
    Preconditions.checkNotNull(messages, "messages");

    ProxiedPlayer proxyPlayer = getProxy().getPlayer(playerName);
    if (proxyPlayer != null)
    {
      for (String message : messages)
      {
        //noinspection deprecation
        proxyPlayer.sendMessage(message);
      }
    }
  }

  @Override
  public void sendChatMessagesToConsole(List<String> messages)
  {
    Preconditions.checkNotNull(messages, "messages");

    CommandSender console = getProxy().getConsole();
    for (String message : messages)
    {
      //noinspection deprecation
      console.sendMessage(message);
    }
  }

  private boolean reloadConfiguration()
  {
    ConfigurationProvider yamlProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
    File file = BungeeResourceUtil.saveResource(this, "bungee-config.yml", "config.yml");

    try
    {
      Configuration loadedConfig = yamlProvider.load(file);
      configuration.read(loadedConfig);
      return true;
    }
    catch (IOException ex)
    {
      getLogger().severe("============================================================");
      getLogger().severe("The SockExchange configuration file could not be loaded.");
      ex.printStackTrace();
      getLogger().severe("============================================================");
    }

    return false;
  }

  private void checkForConsumerTimeouts()
  {
    long currentTimeMillis = System.currentTimeMillis();

    responseConsumerMap.removeIf((entry) ->
    {
      ExpirableConsumer<ResponseMessage> responseConsumer = entry.getValue();

      if (responseConsumer.getExpiresAtMillis() > currentTimeMillis)
      {
        // Keep the entry
        return false;
      }

      awaitableExecutor.execute(() ->
      {
        ResponseMessage responseMessage = new ResponseMessage(ResponseStatus.TIMED_OUT);
        responseConsumer.accept(responseMessage);
      });

      // Remove the entry
      return true;
    });
  }

  private void shutdownAwaitableExecutor()
  {
    try
    {
      awaitableExecutor.setAcceptingTasks(false);
      awaitableExecutor.awaitTasksWithSleep(10, 1000);
      awaitableExecutor.shutdown();
    }
    catch (InterruptedException ex)
    {
      ex.printStackTrace();
    }
  }

  private void buildThreadPoolExecutor()
  {
    ThreadFactoryBuilder factoryBuilder = new ThreadFactoryBuilder();
    factoryBuilder.setNameFormat("SockExchange-Scheduler-Thread-%d");

    ThreadFactory threadFactory = factoryBuilder.build();
    threadPoolExecutor = new ScheduledThreadPoolExecutor(2, threadFactory);

    threadPoolExecutor.setMaximumPoolSize(8);
    threadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
    threadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
  }

  private void shutdownThreadPoolExecutor()
  {
    if (!threadPoolExecutor.isShutdown())
    {
      // Disable new tasks from being submitted to service
      threadPoolExecutor.shutdown();

      getLogger().info("ScheduledThreadPoolExecutor being shutdown()");

      try
      {
        // Await termination for a minute
        if (!threadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS))
        {
          // Force shutdown
          threadPoolExecutor.shutdownNow();

          getLogger().severe("ScheduledThreadPoolExecutor being shutdownNow()");

          // Await termination again for another minute
          if (!threadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS))
          {
            getLogger().severe("ScheduledThreadPoolExecutor not shutdown after shutdownNow()");
          }
        }
      }
      catch (InterruptedException ex)
      {
        getLogger().severe("ScheduledThreadPoolExecutor shutdown interrupted");

        // Re-cancel if current thread also interrupted
        threadPoolExecutor.shutdownNow();

        getLogger().severe("ScheduledThreadPoolExecutor being shutdownNow()");

        // Preserve interrupt status
        Thread.currentThread().interrupt();
      }
    }
  }
}
