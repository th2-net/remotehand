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

import com.exactprosystems.remotehand.sessions.DownloadHandler;
import com.exactprosystems.remotehand.sessions.SessionExchange;
import com.exactprosystems.remotehand.sessions.SessionHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpDownloadHandler implements HttpHandler
{
	private static final String FILE_TYPE_PARAM = "type",
			FILE_ID_PARAM = "id",
			REQUIRED_PARAM_ABSENT = "Required parameter '%s' isn't specified";
	
	private final DownloadHandler downloadHandler;
	
	public HttpDownloadHandler(DownloadHandler downloadHandler)
	{
		this.downloadHandler = downloadHandler;
	}
	
	
	@Override
	public void handle(HttpExchange httpExchange) throws IOException
	{
		Map<String, String> requestParams = getRequestParams(httpExchange);
		SessionExchange exchange = new HttpSessionExchange(httpExchange);
		
		String fileType = getRequiredParam(requestParams, exchange, FILE_TYPE_PARAM);
		if (fileType == null)
			return;
		String fileId = getRequiredParam(requestParams, exchange, FILE_ID_PARAM);
		if (fileId == null)
			return;
		
		downloadHandler.handleDownload(exchange, fileType, fileId);
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
	
	private String getRequiredParam(Map<String, String> requestParams, SessionExchange exchange, String paramName)
		throws IOException
	{
		String value = requestParams.get(paramName);
		if (value == null)
			exchange.sendResponse(SessionHandler.CODE_BAD, String.format(REQUIRED_PARAM_ABSENT, paramName));
		return value;
	}
}
