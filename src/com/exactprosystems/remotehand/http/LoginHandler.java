/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.http;

import java.io.IOException;
import java.io.OutputStream;

import com.exactprosystems.remotehand.IRemoteHandManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;

import static java.util.UUID.randomUUID;

public class LoginHandler implements HttpHandler
{
	private static final Logger logger = Logger.getLogger(LoginHandler.class);
	private IRemoteHandManager manager;

	public LoginHandler (IRemoteHandManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(HttpExchange exchanger) throws IOException
	{
		logger.info("Accepted connection from " + exchanger.getRemoteAddress().getAddress());

		final String sessionId = "/" + createSessionId();

		HTTPServer.getServer().createContext(sessionId, new SessionHandler(sessionId, manager));

		this.sendResponse(exchanger, sessionId);
	}

	private String createSessionId()
	{
		return "Ses" + randomUUID().toString();
	}

	protected void sendResponse(HttpExchange exchange, String sessionId) throws IOException {
		exchange.sendResponseHeaders(200, sessionId.length());
		OutputStream os = exchange.getResponseBody();
		os.write(sessionId.getBytes());
		os.close();
	}


}
