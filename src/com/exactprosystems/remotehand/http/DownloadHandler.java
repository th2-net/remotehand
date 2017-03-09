////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.remotehand.http;

import com.exactprosystems.remotehand.web.WebConfiguration;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author anna.bykova.
 */
public class DownloadHandler implements HttpHandler
{
	private static final String FILE_TYPE_PARAM = "type";
	private static final String FILE_ID_PARAM = "id";
	
	private static final String SCREENSHOT_FILE_TYPE = "screenshot";
	
	private static final String REQUIRED_PARAM_ABSENT = "Required parameter '%s' isn't specified";
	private static final String UNKNOWN_FILE_TYPE = "Unknown file type '%s'";
	private static final String RESOURCE_NOT_FOUND = "Specified resource '%s' not found";

	@Override
	public void handle(HttpExchange httpExchange) throws IOException
	{
		Map<String, String> requestParams = getRequestParams(httpExchange);
		
		String fileType = getRequiredParam(requestParams, httpExchange, FILE_TYPE_PARAM);
		if (fileType == null)
			return;
		String fileId = getRequiredParam(requestParams, httpExchange, FILE_ID_PARAM);
		if (fileId == null)
			return;
		
		if (SCREENSHOT_FILE_TYPE.equalsIgnoreCase(fileType))
			sendScreenshot(httpExchange, fileId);
		else 
			sendResponse(httpExchange, 400, format(UNKNOWN_FILE_TYPE, fileType));
	}
	
	private Map<String, String> getRequestParams(HttpExchange httpExchange)
	{
		List<NameValuePair> pairs = URLEncodedUtils.parse(httpExchange.getRequestURI(), "UTF8");
		Map<String, String> params = new HashMap<>(pairs.size());
		for (NameValuePair pair : pairs)
		{
			params.put(pair.getName(), pair.getValue());
		}
		return params;
	}
	
	private void sendResponse(HttpExchange exchanger, int code, String message) throws IOException
	{
		exchanger.sendResponseHeaders(code, message.length());
		try (OutputStream os = exchanger.getResponseBody())
		{
			os.write(message.getBytes());
		}
	}
	
	private String getRequiredParam(Map<String, String> requestParams, HttpExchange httpExchange, String paramName)
		throws IOException
	{
		String value = requestParams.get(paramName);
		if (value == null)
			sendResponse(httpExchange, 400, format(REQUIRED_PARAM_ABSENT, paramName));
		return value;
	}
	
	private void sendScreenshot(HttpExchange exchanger, String fileId) throws IOException
	{
		File file = new File(WebConfiguration.SCREENSHOTS_DIR_NAME, fileId);
		if (file.exists())
		{
			exchanger.sendResponseHeaders(200, file.length());
			exchanger.getResponseHeaders().set("Content-Type", "image/png");
			exchanger.getResponseHeaders().set("Content-Disposition", "attachment; filename=" + fileId);
			try (OutputStream os = exchanger.getResponseBody())
			{
				Files.copy(file.toPath(), os);
			}
		}
		else 
			sendResponse(exchanger, 404, format(RESOURCE_NOT_FOUND, fileId));
	}
}