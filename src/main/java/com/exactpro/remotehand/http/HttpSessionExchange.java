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

import com.exactpro.remotehand.sessions.SessionExchange;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Adapter for HttpExchange to handle HTTP requests
 */
public class HttpSessionExchange implements SessionExchange
{
	private final HttpExchange exchange;
	
	public HttpSessionExchange(HttpExchange exchange)
	{
		this.exchange = exchange;
	}
	
	
	@Override
	public void sendResponse(int code, String message) throws IOException
	{
		byte[] messageBytes = message.getBytes();
		exchange.sendResponseHeaders(code, messageBytes.length);
		try (OutputStream os = exchange.getResponseBody())
		{
			os.write(messageBytes);
		}
	}
	
	@Override
	public void sendFile(int code, File f, String type, String name) throws IOException
	{
		exchange.getResponseHeaders().set("Content-Type", type);
		exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename="+name);
		exchange.sendResponseHeaders(200, f.length());
		try (OutputStream os = exchange.getResponseBody())
		{
			Files.copy(f.toPath(), os);
		}
	}
	
	@Override
	public String getRemoteAddress()
	{
		return exchange.getRemoteAddress().getAddress().toString();
	}

	public Map<String, String> getRequestParams()
	{
		String query = exchange.getRequestURI().getQuery();

		if (isEmpty(query))
			return null;

		Map<String, String> params = new HashMap<>();
		String[] splitQuery =  query.split("&");

		for (String kvPair : splitQuery)
		{
			String[] splitPairs = kvPair.split("=");

			if (splitPairs.length != 2)
				continue;

			params.put(splitPairs[0], splitPairs[1]);
		}

		return params;
	}
}
