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

package com.exactpro.remotehand.http;

import com.exactpro.remotehand.Configuration;
import com.exactpro.remotehand.sessions.DownloadHandler;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

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
