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
package com.gmail.tracebachi.SockExchange.Messages;

import com.gmail.tracebachi.SockExchange.Utilities.ExtraPreconditions;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class ReceivedMessage
{
  private static final byte[] SINGLE_BYTE_RESPONSE = { 0 };

  private final String channelName;
  private final byte[] messageBytes;
  private final Consumer<byte[]> onResponseConsumer;
  private final AtomicBoolean canRespond;

  public ReceivedMessage(
    String channelName, byte[] messageBytes, Consumer<byte[]> onResponseConsumer)
  {
    ExtraPreconditions.checkNotEmpty(channelName, "channelName");
    Preconditions.checkNotNull(messageBytes, "messageBytes");

    this.channelName = channelName;
    this.messageBytes = messageBytes;
    this.onResponseConsumer = onResponseConsumer;
    this.canRespond = new AtomicBoolean(onResponseConsumer != null);
  }

  public String getChannelName()
  {
    return channelName;
  }

  public byte[] getMessageBytes()
  {
    return messageBytes;
  }

  public ByteArrayDataInput getDataInput()
  {
    if (messageBytes == null)
    {
      return ByteStreams.newDataInput(new byte[0]);
    }

    return ByteStreams.newDataInput(messageBytes);
  }

  /**
   * @return True if a response can be sent
   */
  public boolean canRespond()
  {
    return canRespond.get();
  }

  /**
   * Sends a response of one byte to the server that sent the message
   * <p>
   * This method can be used to send a message that "confirms" that
   * a message was received. Only one response can be sent.
   *
   * @return True if a response was sent or false if not
   */
  public boolean respond()
  {
    return respond(SINGLE_BYTE_RESPONSE);
  }

  /**
   * Sends a response to the server that sent the message
   * <p>
   * Only one response can be sent.
   *
   * @param responseBytes Bytes to respond with
   *
   * @return True if a response was sent or false if not
   */
  public boolean respond(byte[] responseBytes)
  {
    Preconditions.checkNotNull(responseBytes, "responseBytes");

    if (onResponseConsumer != null && canRespond.compareAndSet(true, false))
    {
      onResponseConsumer.accept(responseBytes);
      return true;
    }

    return false;
  }
}
