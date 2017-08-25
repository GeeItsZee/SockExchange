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
package com.gmail.tracebachi.SockExchange.Scheduler;

import com.google.common.base.Preconditions;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class AwaitableExecutor implements Executor
{
  private static final byte ACCEPTING = 1;
  private static final byte NOT_ACCEPTING = 2;
  private static final byte SHUTDOWN = 3;

  private final Object lock = new Object();
  private final ExecutorService executorService;
  private final ConcurrentHashMap.KeySetView<Integer, Boolean> submittedTaskSet;
  private int taskCounter = 1;
  private byte stateByte = ACCEPTING;

  public AwaitableExecutor(ExecutorService executorService)
  {
    Preconditions.checkNotNull(executorService, "executorService");

    this.executorService = executorService;
    this.submittedTaskSet = ConcurrentHashMap.newKeySet();
  }

  /**
   * @param runnable Task to execute
   */
  @Override
  public void execute(Runnable runnable)
  {
    submit(runnable);
  }

  /**
   * @param runnable Task to execute
   * @return True if submitted or false
   */
  public boolean submit(Runnable runnable)
  {
    synchronized (lock)
    {
      if (stateByte != ACCEPTING)
      {
        return false;
      }

      // If the underlying executorService has shutdown, we must shutdown
      // this executor.
      if (executorService.isShutdown())
      {
        shutdown();
        return false;
      }

      int taskId = taskCounter++;
      Runnable wrappedTask = wrapTask(runnable, taskId);

      submittedTaskSet.add(taskId);
      executorService.execute(wrappedTask);
      return true;
    }
  }

  /**
   * @return If the executor is currently accepting tasks
   */
  public boolean isAcceptingTasks()
  {
    synchronized (lock)
    {
      return stateByte == ACCEPTING;
    }
  }

  /**
   * @param acceptingTasks New value for the executor
   */
  public void setAcceptingTasks(boolean acceptingTasks)
  {
    synchronized (lock)
    {
      if (stateByte == SHUTDOWN)
      {
        // If the new value was going to stop accepting tasks, then
        // shutdown is like a successful value change.
        // If the new value was going to start accepting tasks, then
        // shutdown is like a failed value change.
        return;
      }

      stateByte = (acceptingTasks) ? ACCEPTING : NOT_ACCEPTING;
    }
  }

  /**
   * @return If the executor is shutdown
   */
  public boolean isShutdown()
  {
    synchronized (lock)
    {
      return stateByte == SHUTDOWN;
    }
  }

  /**
   * Shutdown the executor
   * <p>
   * This will prevent the executor from accepting any tasks and will NOT
   * await tasks. The executor will not be able to switch back into
   * accepting tasks.
   * </p>
   */
  public void shutdown()
  {
    synchronized (lock)
    {
      stateByte = SHUTDOWN;
      submittedTaskSet.clear();
    }
  }

  /**
   * Sleep to wait for tasks to finish
   *
   * @param maxLoops Number of times to run the check-sleep loop
   * @param millisToSleep Milliseconds to sleep when waiting
   *
   * @return True if there are no more tasks running or false otherwise
   *
   * @throws InterruptedException If {@link Thread#sleep(long)} is interrupted
   */
  public boolean awaitTasksWithSleep(int maxLoops, long millisToSleep) throws InterruptedException
  {
    int remainingTasks = submittedTaskSet.size();

    if (remainingTasks <= 0)
    {
      return true;
    }

    for (int i = 0; i < maxLoops && remainingTasks > 0; i++)
    {
      // It is documented that we are using a blocking call to Thread.sleep()
      Thread.sleep(millisToSleep);

      remainingTasks = submittedTaskSet.size();
    }

    return remainingTasks <= 0;
  }

  private Runnable wrapTask(Runnable task, int taskId)
  {
    return () -> {
      try
      {
        task.run();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      finally
      {
        submittedTaskSet.remove(taskId);
      }
    };
  }
}
