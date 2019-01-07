/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.IRemoteHandManager;
import com.exactprosystems.remotehand.http.LoginHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by alexey.karpukhin on 10/21/16.
 */
public class WebLoginHandler extends LoginHandler{

	public WebLoginHandler(IRemoteHandManager manager) {
		super(manager);
	}

	private String getUsedBrowser()
	{
		return ((WebConfiguration) Configuration.getInstance()).getBrowserToUse().getLabel();
	}

	@Override
	protected void sendResponse(HttpExchange exchange, String sessionId) throws IOException {
		String responseMsg = String.format("sessionId=%s;browser=%s", sessionId, getUsedBrowser());

		exchange.sendResponseHeaders(200, responseMsg.length());
		OutputStream os = exchange.getResponseBody();
		os.write(responseMsg.getBytes());
		os.close();
	}
}
