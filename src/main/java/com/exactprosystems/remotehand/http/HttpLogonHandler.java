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