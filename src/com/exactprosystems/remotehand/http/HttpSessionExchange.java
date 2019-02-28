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
import java.io.OutputStream;

import com.exactprosystems.remotehand.sessions.SessionExchange;
import com.sun.net.httpserver.HttpExchange;

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
		exchange.sendResponseHeaders(code, message.length());
		OutputStream os = exchange.getResponseBody();
		os.write(message.getBytes());
		os.close();
	}
	
	@Override
	public String getRemoteAddress()
	{
		return exchange.getRemoteAddress().getAddress().toString();
	}
}