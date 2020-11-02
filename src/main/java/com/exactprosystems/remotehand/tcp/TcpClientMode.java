/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
