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

import com.gmail.tracebachi.SockExchange.ExpirableConsumer;
import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessageNotifier;
import com.gmail.tracebachi.SockExchange.Messages.ResponseMessage;
import com.gmail.tracebachi.SockExchange.Messages.ResponseStatus;
import com.gmail.tracebachi.SockExchange.Netty.SockExchangeClient;
import com.gmail.tracebachi.SockExchange.Netty.SpigotToBungeeConnection;
import com.gmail.tracebachi.SockExchange.Scheduler.AwaitableExecutor;
import com.gmail.tracebachi.SockExchange.Scheduler.ScheduledExecutorServiceWrapper;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.gmail.tracebachi.SockExchange.Utilities.BasicLogger;
import com.gmail.tracebachi.SockExchange.Utilities.JulBasicLogger;
import com.gmail.tracebachi.SockExchange.Utilities.LongIdCounterMap;
import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class SockExchangePlugin extends JavaPlugin implements SpigotTieIn
{
  private final SockExchangeConfiguration configuration = new SockExchangeConfiguration();

  private ScheduledThreadPoolExecutor threadPoolExecutor;
  private AwaitableExecutor awaitableExecutor;
  private BasicLogger basicLogger;
  private ReceivedMessageNotifier messageNotifier;
  private LongIdCounterMap<ExpirableConsumer<ResponseMessage>> responseConsumerMap;
  private SpigotToBungeeConnection connection;
  private SockExchangeClient sockExchangeClient;
  private ScheduledFuture<?> consumerTimeoutCleanupFuture;

  private PlayerUpdateChannelListener playerUpdateChannelListener;
  private KeepAliveChannelListener keepAliveChannelListener;
  private MoveOtherToCommand moveOtherToCommand;
  private MoveToCommand moveToCommand;
  private RunCmdCommand runCmdCommand;
  private ChatMessageChannelListener chatMessageChannelListener;
  private RunCmdChannelListener runCmdChannelListener;
  private SpigotKeepAliveSender spigotKeepAliveSender;

  @Override
  public void onLoad()
  {
    saveDefaultConfig();
  }

  @Override
  public void onEnable()
  {
    reloadConfig();
    configuration.read(getConfig());

    boolean debugMode = configuration.inDebugMode();
    String hostName = configuration.getHostName();
    int port = configuration.getPort();
    String serverName = configuration.getServerName();
    String registrationPassword = configuration.getRegistrationPassword();
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

    // Create the Spigot-to-Bungee connection
    connection = new SpigotToBungeeConnection(
      serverName, registrationPassword, awaitableExecutor, messageNotifier, responseConsumerMap,
      basicLogger);

    // Create the API
    SockExchangeApi api = new SockExchangeApi(
      this, threadPoolExecutor, messageNotifier, connection);
    SockExchangeApi.setInstance(api);

    playerUpdateChannelListener = new PlayerUpdateChannelListener(api);
    playerUpdateChannelListener.register();

    keepAliveChannelListener = new KeepAliveChannelListener(api);
    keepAliveChannelListener.register();

    moveOtherToCommand = new MoveOtherToCommand(this, messageFormatMap, api);
    moveOtherToCommand.register();

    moveToCommand = new MoveToCommand(this, messageFormatMap, api);
    moveToCommand.register();

    runCmdChannelListener = new RunCmdChannelListener(this, basicLogger, api);
    runCmdChannelListener.register();

    runCmdCommand = new RunCmdCommand(this, runCmdChannelListener, messageFormatMap, api);
    runCmdCommand.register();

    chatMessageChannelListener = new ChatMessageChannelListener(this, api);
    chatMessageChannelListener.register();

    spigotKeepAliveSender = new SpigotKeepAliveSender(api, 2000);
    spigotKeepAliveSender.register();

    try
    {
      sockExchangeClient = new SockExchangeClient(hostName, port, connection);
      sockExchangeClient.start();
    }
    catch (Exception e)
    {
      getLogger().severe("============================================================");
      getLogger().severe("The SockExchange client could not be started. Refer to the stacktrace below.");
      getLogger().severe("Regardless, reconnects will be attempted.");
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

    if (sockExchangeClient != null)
    {
      sockExchangeClient.shutdown();
      sockExchangeClient = null;
    }

    if (spigotKeepAliveSender != null)
    {
      spigotKeepAliveSender.unregister();
      spigotKeepAliveSender = null;
    }

    if (chatMessageChannelListener != null)
    {
      chatMessageChannelListener.unregister();
      chatMessageChannelListener = null;
    }

    if (runCmdCommand != null)
    {
      runCmdCommand.unregister();
      runCmdCommand = null;
    }

    if (runCmdChannelListener != null)
    {
      runCmdChannelListener.unregister();
      runCmdChannelListener = null;
    }

    if (moveToCommand != null)
    {
      moveToCommand.unregister();
      moveToCommand = null;
    }

    if (moveOtherToCommand != null)
    {
      moveOtherToCommand.unregister();
      moveOtherToCommand = null;
    }

    if (keepAliveChannelListener != null)
    {
      keepAliveChannelListener.unregister();
      keepAliveChannelListener = null;
    }

    if (playerUpdateChannelListener != null)
    {
      playerUpdateChannelListener.unregister();
      playerUpdateChannelListener = null;
    }

    SockExchangeApi.setInstance(null);

    connection = null;

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

    messageNotifier = null;
    basicLogger = null;

    if (threadPoolExecutor != null)
    {
      shutdownThreadPoolExecutor();
      threadPoolExecutor = null;
    }
  }

  @Override
  public SpigotServerInfo getServerInfo(String serverName)
  {
    return keepAliveChannelListener.getServerInfo(serverName);
  }

  @Override
  public Collection<SpigotServerInfo> getServerInfos()
  {
    return keepAliveChannelListener.getServerInfos();
  }

  @Override
  public Set<String> getOnlinePlayerNames()
  {
    return playerUpdateChannelListener.getOnlinePlayerNames();
  }

  @Override
  public void sendChatMessagesToConsole(List<String> messages)
  {
    getServer().getScheduler().runTask(this, () ->
    {
      CommandSender receiver = getServer().getConsoleSender();
      for (String message : messages)
      {
        receiver.sendMessage(message);
      }
    });
  }

  @Override
  public void isPlayerOnServer(String playerName, Consumer<Boolean> consumer)
  {
    getServer().getScheduler().runTask(this, () ->
    {
      Player player = getServer().getPlayerExact(playerName);
      consumer.accept(player != null);
    });
  }

  public void executeSync(Runnable runnable)
  {
    Preconditions.checkNotNull(runnable, "runnable");

    getServer().getScheduler().runTask(this, runnable);
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
