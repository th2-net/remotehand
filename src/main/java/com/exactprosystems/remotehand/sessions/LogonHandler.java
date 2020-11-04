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

package com.exactprosystems.remotehand.sessions;

import java.io.IOException;

import com.exactprosystems.remotehand.IRemoteHandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.UUID.randomUUID;

public abstract class LogonHandler
{
	private static final Logger logger = LoggerFactory.getLogger(LogonHandler.class);
	private IRemoteHandManager manager;
	
	public LogonHandler(IRemoteHandManager manager)
	{
		this.manager = manager;
	}
	
	public abstract void handleLogon(String sessionId, SessionExchange exchange);
	
	public void handleLogon(SessionExchange exchange) throws IOException
	{
		logger.info("Accepted connection from " + exchange.getRemoteAddress());
		String sessionId = "/" + createSessionId();
		handleLogon(sessionId, exchange);
		sendResponse(exchange, sessionId);
	}
	
	private String createSessionId()
	{
		return "Ses" + randomUUID().toString();
	}
	
	protected void sendResponse(SessionExchange exchange, String response) throws IOException
	{
		exchange.sendResponse(SessionHandler.CODE_SUCCESS, response);
	}
	
	public IRemoteHandManager getManager()
	{
		return manager;
	}
}
