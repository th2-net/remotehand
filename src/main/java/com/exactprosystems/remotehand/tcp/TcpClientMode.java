/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactprosystems.remotehand.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.IRemoteHandManager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TcpClientMode
{
	private static final Logger logger = LoggerFactory.getLogger(TcpClientMode.class);
	private static final String TCP_HOST = Configuration.getInstance().getHost();
	private static final int TCP_PORT = Configuration.getInstance().getPort();
	
	private static volatile ChannelFuture clientFuture = null;

	public static boolean init(final String version, final IRemoteHandManager manager)
	{
		if (clientFuture != null)
			return false;
		
		TcpSessions.init();
		
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try
		{
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.handler(new ChannelInitializer<SocketChannel>()
			{
				@Override
				public void initChannel(SocketChannel ch) throws Exception
				{
					ch.pipeline().addLast(
							new TcpDecoder(),
							new TcpClientHandler(version, manager));
				}
			});
			
			// Connecting to external application
			clientFuture = b.connect(TCP_HOST, TCP_PORT).sync();
			return true;
		}
		catch (Exception e)
		{
			logger.error("Could not connect to ClearTH ("+TCP_HOST+":"+TCP_PORT+")", e);
			return false;
		}
	}
}
