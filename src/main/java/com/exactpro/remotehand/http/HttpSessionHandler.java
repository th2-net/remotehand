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

import com.exactpro.remotehand.IRemoteHandManager;
import com.exactpro.remotehand.requests.*;
import com.exactpro.remotehand.sessions.SessionHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

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
