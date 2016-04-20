////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.http;

import java.io.IOException;
import java.io.OutputStream;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.IRemoteHandManager;
import com.exactprosystems.remotehand.web.WebConfiguration;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;

public class LoginHandler implements HttpHandler
{
	private static final Logger logger = Logger.getLogger(LoginHandler.class);
	private static final LoginHandler handler = new LoginHandler();

	public static LoginHandler getHandler()
	{
		return handler;
	}

	private IRemoteHandManager manager;
	public void setRhManager (IRemoteHandManager irh) {
		this.manager = irh;
	}

	@Override
	public void handle(HttpExchange exchanger) throws IOException
	{
		logger.info("Accepted connection from " + exchanger.getRemoteAddress().getAddress());

		final String sessionId = "/" + createSessionId();

		HTTPServer.getServer().createContext(sessionId, new SessionHandler(sessionId, manager));

		String responseMsg = String.format("sessionId=%s;browser=%s", sessionId, getUsedBrowser());
		
		exchanger.sendResponseHeaders(200, responseMsg.length());
		OutputStream os = exchanger.getResponseBody();
		os.write(responseMsg.getBytes());
		os.close();
	}

	private String createSessionId()
	{
		return "Ses" + Long.toString(System.currentTimeMillis());
	}
	
	private String getUsedBrowser()
	{
		return ((WebConfiguration) Configuration.getInstance()).getBrowserToUse().getLabel();
	}
}
