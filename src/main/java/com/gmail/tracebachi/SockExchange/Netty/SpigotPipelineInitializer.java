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
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class SpigotPipelineInitializer extends ChannelInitializer
{
  private static final String READ_TIMEOUT_HANDLER = "read-timeout-handler";
  private static final String FRAME_DECODER = "frame-decoder";
  private static final String FRAME_PREPENDER = "frame-prepender";
  private static final String PACKET_DECODER = "packet-decoder";
  private static final String PACKET_ENCODER = "packet-encoder";
  private static final int MAX_FRAME_SIZE = 4 * 1024 * 1024; // 4 MB
  private static final int FRAME_LENGTH_FIELD_OFFSET = 0;
  private static final int FRAME_LENGTH_FIELD_LENGTH = 3;
  private static final int FRAME_LENGTH_ADJUSTMENT = 0;

  private final AbstractPacketHandler packetHandler;

  public SpigotPipelineInitializer(AbstractPacketHandler packetHandler)
  {
    Preconditions.checkNotNull(packetHandler, "packetHandler");

    this.packetHandler = packetHandler;
  }

  @Override
  protected void initChannel(Channel channel) throws Exception
  {
    ChannelPipeline pipeline = channel.pipeline();

    // Add a read timeout handler
    ReadTimeoutHandler timeoutHandler = new ReadTimeoutHandler(15, TimeUnit.SECONDS);
    pipeline.addLast(READ_TIMEOUT_HANDLER, timeoutHandler);

    // Add a frame decoder
    LengthFieldBasedFrameDecoder frameDecoder = new LengthFieldBasedFrameDecoder(MAX_FRAME_SIZE,
      FRAME_LENGTH_FIELD_OFFSET, FRAME_LENGTH_FIELD_LENGTH, FRAME_LENGTH_ADJUSTMENT,
      FRAME_LENGTH_FIELD_LENGTH);
    pipeline.addLast(FRAME_DECODER, frameDecoder);

    // Add a frame prepender
    pipeline.addLast(FRAME_PREPENDER, new LengthFieldPrepender(FRAME_LENGTH_FIELD_LENGTH, false));

    // Add a packet decoder and encoder
    pipeline.addLast(PACKET_DECODER, new SpigotPacketDecoder(packetHandler));
    pipeline.addLast(PACKET_ENCODER, new SpigotPacketEncoder());
  }
}
