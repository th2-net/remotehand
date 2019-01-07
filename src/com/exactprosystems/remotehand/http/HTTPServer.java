/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
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
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPServer
{
	private static final Logger logger = LoggerFactory.getLogger(HTTPServer.class);
	private static final int HTTP_SRV_PORT = Configuration.getInstance().getHttpServerPort();
	private static final String LOGIN_LISTENER = "/login";
	private static final String DOWNLOAD_LISTENER = "/download";

	private static volatile HttpServer server = null;

	public static boolean createServer(LoginHandler loginHandler) {
		if (server == null) {
			try {
				server = HttpServer.create(new InetSocketAddress(HTTP_SRV_PORT), 0);
				server.createContext(LOGIN_LISTENER, loginHandler);
				server.createContext(DOWNLOAD_LISTENER, new DownloadHandler());
				server.setExecutor(null); // creates a default executor
				server.start();

				logger.info(String.format("HTTP Server started on port <%s>", HTTP_SRV_PORT));
			} catch (IOException ex) {
				logger.error(String.format("Could not create HTTP Server on port <%s>", HTTP_SRV_PORT), ex);
			}
			return server != null;
		} else {
			return false;
		}
	}

	public static HttpServer getServer()
	{
		return server;
	}
}
