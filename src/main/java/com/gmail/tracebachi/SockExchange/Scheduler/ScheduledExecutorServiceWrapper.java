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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class ScheduledExecutorServiceWrapper implements ScheduledExecutorService
{
  private final ScheduledThreadPoolExecutor executor;

  public ScheduledExecutorServiceWrapper(ScheduledThreadPoolExecutor executor)
  {
    Preconditions.checkNotNull(executor, "executor");

    this.executor = executor;
  }

  @Override
  public ScheduledFuture<?> schedule(
    Runnable runnable, long delay, TimeUnit timeUnit)
  {
    return executor.schedule(runnable, delay ,timeUnit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(
    Callable<V> callable, long delay, TimeUnit timeUnit)
  {
    return executor.schedule(callable, delay, timeUnit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(
    Runnable runnable, long initialDelay, long period, TimeUnit timeUnit)
  {
    return executor.scheduleWithFixedDelay(runnable, initialDelay, period, timeUnit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(
    Runnable runnable, long initialDelay, long periodDelay, TimeUnit timeUnit)
  {
    return scheduleWithFixedDelay(runnable, initialDelay, periodDelay, timeUnit);
  }

  @Override
  public void shutdown()
  {
    throw new UnsupportedOperationException("Unsafe operation");
  }

  @Override
  public List<Runnable> shutdownNow()
  {
    throw new UnsupportedOperationException("Unsafe operation");
  }

  @Override
  public boolean isShutdown()
  {
    return executor.isShutdown();
  }

  @Override
  public boolean isTerminated()
  {
    return executor.isTerminated();
  }

  @Override
  public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException
  {
    throw new UnsupportedOperationException("Unsafe operation");
  }

  @Override
  public <T> Future<T> submit(Callable<T> callable)
  {
    return executor.submit(callable);
  }

  @Override
  public <T> Future<T> submit(Runnable runnable, T t)
  {
    return executor.submit(runnable, t);
  }

  @Override
  public Future<?> submit(Runnable runnable)
  {
    return executor.submit(runnable);
  }

  @Override
  public <T> List<Future<T>> invokeAll(
    Collection<? extends Callable<T>> collection) throws InterruptedException
  {
    return executor.invokeAll(collection);
  }

  @Override
  public <T> List<Future<T>> invokeAll(
    Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit)
    throws InterruptedException
  {
    return executor.invokeAll(collection, l, timeUnit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> collection)
    throws InterruptedException, ExecutionException
  {
    return executor.invokeAny(collection);
  }

  @Override
  public <T> T invokeAny(
    Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit)
    throws InterruptedException, ExecutionException, TimeoutException
  {
    return executor.invokeAny(collection, l, timeUnit);
  }

  @Override
  public void execute(Runnable runnable)
  {
    executor.execute(runnable);
  }
}
