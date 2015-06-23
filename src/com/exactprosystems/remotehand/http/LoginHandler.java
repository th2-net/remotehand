////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.http;

import java.io.IOException;
import java.io.OutputStream;

import com.exactprosystems.remotehand.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class LoginHandler implements HttpHandler
{
	private static final Logger logger = new Logger();
	private static final LoginHandler handler = new LoginHandler();

	public static LoginHandler getHandler()
	{
		return handler;
	}

	@Override
	public void handle(HttpExchange exchanger) throws IOException
	{
		logger.info("Accepted connection from " + exchanger.getRemoteAddress().getAddress());

		final String sessionId = "/" + createSessionId();

		HTTPServer.getServer().createContext(sessionId, new SessionHandler(sessionId));

		exchanger.sendResponseHeaders(200, sessionId.length());
		OutputStream os = exchanger.getResponseBody();
		os.write(sessionId.getBytes());
		os.close();
	}

	private String createSessionId()
	{
		return "Ses" + Long.toString(System.currentTimeMillis());
	}
}
