/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.grid;

import com.exactprosystems.remotehand.IRemoteHandManager;
import com.exactprosystems.remotehand.RemoteManagerType;
import com.exactprosystems.remotehand.http.HTTPServerMode;
import com.exactprosystems.remotehand.http.HttpSessionExchange;
import com.exactprosystems.remotehand.http.HttpSessionHandler;
import com.exactprosystems.remotehand.sessions.SessionExchange;
import com.exactprosystems.remotehand.sessions.SessionHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static java.util.UUID.randomUUID;

public class GridLogonHandler implements HttpHandler
{
	private static final Logger logger = LoggerFactory.getLogger(GridLogonHandler.class);

	private static final String MANAGER_TYPE = "manager";
	private static final String TARGET_DRIVER = "target";

	private final GridRemoteHandManager handManager;


	public GridLogonHandler(GridRemoteHandManager handManager)
	{
		this.handManager = handManager;
	}


	@Override
	public void handle(HttpExchange exchange) throws IOException
	{
		HttpSessionExchange httpSessionExchange = new HttpSessionExchange(exchange);
		logger.info("Accepted connection from {}", exchange.getRemoteAddress());
		HttpSessionHandler sessionHandler = createSessionHandler(httpSessionExchange);
		bindSession(sessionHandler);
		sendResponse(httpSessionExchange, sessionHandler.getId());
	}


	protected void sendResponse(SessionExchange exchange, String response) throws IOException
	{
		exchange.sendResponse(SessionHandler.CODE_SUCCESS, response);
	}

	protected HttpSessionHandler createSessionHandler(HttpSessionExchange exchange)
	{
		IRemoteHandManager remoteHandManager = getRemoteHandManager(exchange);
		String sessionUrl = createSessionUrl();
		handManager.saveSession(sessionUrl, getUrlFromRequest(exchange.getRequestParams()));
		return new HttpSessionHandler(sessionUrl, remoteHandManager);
	}

	protected IRemoteHandManager getRemoteHandManager(HttpSessionExchange exchange)
	{
		RemoteManagerType managerType = getManagerType(exchange.getRequestParams());
		return handManager.getRemoteHandManager(managerType);
	}

	protected void bindSession(HttpSessionHandler sessionHandler)
	{
		HttpServer server = HTTPServerMode.getServer();
		server.createContext(sessionHandler.getId(), sessionHandler);
	}


	private String createSessionUrl()
	{
		return "/Ses" + randomUUID();
	}

	private RemoteManagerType getManagerType(Map<String, String> requestParams)
	{
		String managerType = requestParams.get(MANAGER_TYPE);
		return RemoteManagerType.getByLabel(managerType);
	}

	private String getUrlFromRequest(Map<String, String> requestParams)
	{
		return requestParams.get(TARGET_DRIVER);
	}
}
