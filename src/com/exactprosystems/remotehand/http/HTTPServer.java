////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.http;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.exactprosystems.remotehand.Configuration;
import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;

public class HTTPServer
{
	private final static int HTTP_SRV_PORT = Configuration.getInstance().getHttpServerPort();
	private final static String LOGIN_LISTENER = "/login";

	private static final Logger logger = Logger.getLogger(HTTPServer.class);
	private static volatile HttpServer server = null;

	public static HttpServer getServer()
	{
		if (server == null)
			try
			{
				server = HttpServer.create(new InetSocketAddress(HTTP_SRV_PORT), 0);
				server.createContext(LOGIN_LISTENER, LoginHandler.getHandler());
				server.setExecutor(null); // creates a default executor
				server.start();

				logger.info(String.format("HTTP Server started on port <%s>", HTTP_SRV_PORT));
			}
			catch (IOException ex)
			{
				logger.error(String.format("Could not create HTTP Server on port <%s>", HTTP_SRV_PORT), ex);
			}

		return server;
	}
}
