/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.sessions;

import java.io.IOException;

import com.exactprosystems.remotehand.IRemoteHandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.UUID.randomUUID;

public abstract class LogonHandler
{
	private static final Logger logger = LoggerFactory.getLogger(LogonHandler.class);
	private IRemoteHandManager manager;
	
	public LogonHandler(IRemoteHandManager manager)
	{
		this.manager = manager;
	}
	
	public abstract void handleLogon(String sessionId, SessionExchange exchange);
	
	public void handleLogon(SessionExchange exchange) throws IOException
	{
		logger.info("Accepted connection from " + exchange.getRemoteAddress());
		String sessionId = "/" + createSessionId();
		handleLogon(sessionId, exchange);
		sendResponse(exchange, sessionId);
	}
	
	private String createSessionId()
	{
		return "Ses" + randomUUID().toString();
	}
	
	protected void sendResponse(SessionExchange exchange, String response) throws IOException
	{
		exchange.sendResponse(200, response);
	}
	
	public IRemoteHandManager getManager()
	{
		return manager;
	}
}
