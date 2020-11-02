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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.exactprosystems.remotehand.sessions.SessionExchange;
import com.sun.net.httpserver.HttpExchange;

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