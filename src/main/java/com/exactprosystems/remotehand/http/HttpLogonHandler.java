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

package com.exactprosystems.remotehand.http;

import java.io.IOException;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.IRemoteHandManager;
import com.exactprosystems.remotehand.sessions.LogonHandler;
import com.exactprosystems.remotehand.sessions.SessionExchange;
import com.exactprosystems.remotehand.web.WebConfiguration;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HttpLogonHandler extends LogonHandler implements HttpHandler
{
	public HttpLogonHandler(IRemoteHandManager manager)
	{
		super(manager);
	}
	
	@Override
	public void handleLogon(String sessionId, SessionExchange exchange)
	{
		HTTPServerMode.getServer().createContext(sessionId, new HttpSessionHandler(sessionId, getManager()));
	}
	
	@Override
	protected void sendResponse(SessionExchange exchange, String response) throws IOException
	{
		String responseMsg = String.format("sessionId=%s;browser=%s", response, getUsedBrowser());
		super.sendResponse(exchange, responseMsg);
	}

	
	@Override
	public void handle(HttpExchange exchange) throws IOException
	{
		handleLogon(new HttpSessionExchange(exchange));
	}
	
	
	private String getUsedBrowser()
	{
		Configuration instance = Configuration.getInstance();
		
		if (instance instanceof WebConfiguration) {
			return ((WebConfiguration) instance).getBrowserToUse().getLabel();
		} else {
			return "none";
		}
	}
}
