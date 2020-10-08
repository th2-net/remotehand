/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.http;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.sessions.DownloadHandler;
import com.exactprosystems.remotehand.sessions.LogonHandler;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPServerMode
{
	private static final Logger logger = LoggerFactory.getLogger(HTTPServerMode.class);
	private static final int HTTP_SRV_PORT = Configuration.getInstance().getPort();
	private static final String LOGIN_LISTENER = "/login";
	private static final String DOWNLOAD_LISTENER = "/download";

	private static volatile HttpServer server = null;

	public static boolean init(HttpHandler logonHandler) {
		if (server != null)
			return false;
		
		try {
			server = HttpServer.create(new InetSocketAddress(HTTP_SRV_PORT), 0);
			server.createContext(LOGIN_LISTENER, logonHandler);
			server.createContext(DOWNLOAD_LISTENER, new HttpDownloadHandler(new DownloadHandler()));
			server.setExecutor(null); // creates a default executor
			server.start();

			logger.info(String.format("HTTP Server started on port <%s>", HTTP_SRV_PORT));
		} catch (IOException ex) {
			logger.error(String.format("Could not create HTTP Server on port <%s>", HTTP_SRV_PORT), ex);
		}
		return server != null;
	}

	public static HttpServer getServer()
	{
		return server;
	}
}
