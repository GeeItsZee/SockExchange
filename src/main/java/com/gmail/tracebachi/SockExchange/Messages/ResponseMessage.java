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

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class ResponseMessage
{
  private final ResponseStatus responseStatus;
  private final byte[] messageBytes;

  /**
   * @param responseStatus Status of sending request
   */
  public ResponseMessage(ResponseStatus responseStatus)
  {
    this(responseStatus, null);
  }

  /**
   * @param responseStatus Status of sending request
   * @param messageBytes Message bytes
   */
  public ResponseMessage(ResponseStatus responseStatus, byte[] messageBytes)
  {
    Preconditions.checkNotNull(responseStatus, "responseStatus");

    this.responseStatus = responseStatus;
    this.messageBytes = messageBytes;
  }

  /**
   * @return Status of response
   */
  public ResponseStatus getResponseStatus()
  {
    return responseStatus;
  }

  /**
   * @return Raw bytes from response
   */
  public byte[] getMessageBytes()
  {
    return messageBytes;
  }

  /**
   * @return {@link ByteArrayDataInput} of message bytes
   */
  public ByteArrayDataInput getDataInput()
  {
    if (messageBytes == null)
    {
      return ByteStreams.newDataInput(new byte[0]);
    }

    return ByteStreams.newDataInput(messageBytes);
  }
}
