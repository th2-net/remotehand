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
