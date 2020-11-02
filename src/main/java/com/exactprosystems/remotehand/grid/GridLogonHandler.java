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
import java.util.Arrays;
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
		logger.info("Accepted connection from {}", exchange.getRemoteAddress());
		HttpSessionExchange httpSessionExchange = new HttpSessionExchange(exchange);
		try
		{
			HttpSessionHandler sessionHandler = createSessionHandler(httpSessionExchange);
			bindSession(sessionHandler);
			sendResponse(httpSessionExchange, SessionHandler.CODE_SUCCESS, sessionHandler.getId());
		}
		catch (RequestParametersException e)
		{
			logger.error("Invalid request, sending response about error", e);
			sendResponse(httpSessionExchange, SessionHandler.CODE_BAD, e.getMessage());
		}
	}


	protected void sendResponse(SessionExchange exchange, int code, String response) throws IOException
	{
		exchange.sendResponse(code, response);
	}

	protected HttpSessionHandler createSessionHandler(HttpSessionExchange exchange) throws RequestParametersException
	{
		Map<String, String> params = exchange.getRequestParams();
		IRemoteHandManager remoteHandManager = getRemoteHandManager(params);
		String sessionUrl = createSessionUrl();
		handManager.saveSession(sessionUrl, getUrlFromRequest(params));
		return new HttpSessionHandler(sessionUrl, remoteHandManager);
	}

	protected IRemoteHandManager getRemoteHandManager(Map<String, String> requestParams) throws RequestParametersException
	{
		RemoteManagerType managerType = getManagerType(requestParams);
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

	private RemoteManagerType getManagerType(Map<String, String> requestParams) throws RequestParametersException
	{
		String managerType = requestParams != null ? requestParams.get(MANAGER_TYPE) : null;
		if (managerType == null)
			throw new RequestParametersException("'"+MANAGER_TYPE+"' request parameter is mandatory, supported values are "+Arrays.toString(RemoteManagerType.labels()));
		return RemoteManagerType.getByLabel(managerType);
	}

	private String getUrlFromRequest(Map<String, String> requestParams) throws RequestParametersException
	{
		String target = requestParams != null ? requestParams.get(TARGET_DRIVER) : null;
		if (target == null)
			throw new RequestParametersException("'"+TARGET_DRIVER+"' request parameter is mandatory and should be the URL of target remote driver");
		return target;
	}
}
