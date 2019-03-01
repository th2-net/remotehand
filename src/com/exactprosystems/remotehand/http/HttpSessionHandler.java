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

import java.io.*;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.*;
import com.exactprosystems.remotehand.requests.ExecutionRequest;
import com.exactprosystems.remotehand.requests.ExecutionStatusRequest;
import com.exactprosystems.remotehand.requests.FileUploadRequest;
import com.exactprosystems.remotehand.requests.LogoutRequest;
import com.exactprosystems.remotehand.requests.RhRequest;
import com.exactprosystems.remotehand.sessions.SessionHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpSessionHandler extends SessionHandler implements HttpHandler
{
	private static final Logger logger = LoggerFactory.getLogger(HttpSessionHandler.class);
	
	public HttpSessionHandler(String id, IRemoteHandManager manager)
	{
		super(id, manager);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException
	{
		RhRequest request = getRequest(exchange);
		if (request == null)
		{
			logger.warn("Unrecognized request: {}", exchange);
			return;
		}
		
		handle(request, new HttpSessionExchange(exchange));
	}
	
	@Override
	protected void closeConnection() throws IllegalArgumentException
	{
		HTTPServerMode.getServer().removeContext(getId());
	}
	
	
	private RhRequest getRequest(HttpExchange exchange) throws IOException
	{
		String method = exchange.getRequestMethod();
		if (method.equalsIgnoreCase("POST"))
		{
			List<String> contTypeList = exchange.getRequestHeaders().get("Content-type");
			List<String> transFNs = exchange.getRequestHeaders().get("Transfer-filename");
			if (contTypeList != null && !contTypeList.isEmpty() && "application/octet-stream".equals(contTypeList.get(0)) 
					&& transFNs != null && !transFNs.isEmpty())
				return new FileUploadRequest(transFNs.get(0), IOUtils.toByteArray(exchange.getRequestBody()));
			
			String body = IOUtils.toString(exchange.getRequestBody(), UTF_8);
			return new ExecutionRequest(body);
		}
		else if (method.equalsIgnoreCase("GET"))
			return new ExecutionStatusRequest();
		else if (method.equals("DELETE"))
			return new LogoutRequest();
		return null;
	}
}