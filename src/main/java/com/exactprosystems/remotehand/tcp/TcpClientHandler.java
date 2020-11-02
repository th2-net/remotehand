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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.IRemoteHandManager;
import com.exactprosystems.remotehand.requests.DownloadRequest;
import com.exactprosystems.remotehand.requests.LogonRequest;
import com.exactprosystems.remotehand.requests.RhRequest;
import com.exactprosystems.remotehand.sessions.DownloadHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TcpClientHandler extends ChannelInboundHandlerAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(TcpClientHandler.class);
	private final String version;
	private final TcpLogonHandler logonHandler;
	private final DownloadHandler downloadHandler;
	
	public TcpClientHandler(String version, IRemoteHandManager manager)
	{
		this.version = version;
		this.logonHandler = new TcpLogonHandler(manager);
		this.downloadHandler = new DownloadHandler();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		//Once connected, let's tell the server who we are
		new TcpSessionExchange(ctx).sendResponse(0, "RemoteHand "+version);
		logger.info("Connected to host "+ctx.channel().remoteAddress());
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		logger.info("Disconnected from host");
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
	{
		if (!(msg instanceof TcpRequest))
		{
			logger.warn("Unexpected object received: "+msg);
			return;
		}
		
		TcpRequest request = (TcpRequest)msg;
		RhRequest rhRequest = request.getRequest();
		if (rhRequest instanceof LogonRequest)
		{
			handleLogon(ctx);
			return;
		}
		else if (rhRequest instanceof DownloadRequest)
		{
			handleDownload((DownloadRequest)rhRequest, ctx);
			return;
		}
		
		String sessionId = request.getSessionId();
		TcpSessionHandler sessionHandler = TcpSessions.getInstance().getSession(sessionId);
		if (sessionHandler == null)
		{
			logger.error("Unknown session '"+sessionId+"'");
			return;
		}
		sessionHandler.handle(rhRequest, new TcpSessionExchange(ctx));
	}
	
	
	private void handleLogon(ChannelHandlerContext ctx) throws IOException
	{
		logonHandler.handleLogon(new TcpSessionExchange(ctx));
	}
	
	private void handleDownload(DownloadRequest request, ChannelHandlerContext ctx) throws IOException
	{
		downloadHandler.handleDownload(new TcpSessionExchange(ctx), request.getFileType(), request.getFileId());
	}
}