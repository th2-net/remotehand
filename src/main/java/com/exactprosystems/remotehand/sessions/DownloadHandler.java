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

import java.io.File;
import java.io.IOException;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.web.WebConfiguration;

public class DownloadHandler
{
	private static final String SCREENSHOT_FILE_TYPE = "screenshot",
			DOWNLOADED_FILE_TYPE = "downloaded",
			UNKNOWN_FILE_TYPE = "Unknown file type '%s'",
			RESOURCE_NOT_FOUND = "Specified resource '%s' not found";
	
	public void handleDownload(SessionExchange exchange, String fileType, String fileId) throws IOException
	{
		if (SCREENSHOT_FILE_TYPE.equalsIgnoreCase(fileType))
			sendScreenshot(exchange, fileId);
		else if (DOWNLOADED_FILE_TYPE.equalsIgnoreCase(fileType)) 
			sendFile(exchange, fileId);
		else
			exchange.sendResponse(SessionHandler.CODE_BAD, String.format(UNKNOWN_FILE_TYPE, fileType));
	}
	
	private void sendScreenshot(SessionExchange exchange, String fileId) throws IOException
	{
		File file = new File(WebConfiguration.SCREENSHOTS_DIR_NAME, fileId);
		sendFile(exchange, file, "image/png", fileId, fileId);
	}

	private void sendFile(SessionExchange exchange, String fileLocation) throws IOException
	{
		File downloadDir = WebConfiguration.getInstance().getDownloadsDir();
		File file = new File(downloadDir, fileLocation);
		sendFile(exchange, file, "application/octet-stream", fileLocation, fileLocation);
	}
	
	private void sendFile(SessionExchange exchange, File file, String type, String name, String id) throws IOException
	{
		if (file.exists())
			exchange.sendFile(SessionHandler.CODE_SUCCESS, file, type, name);
		else
			exchange.sendResponse(SessionHandler.CODE_NOTFOUND, String.format(RESOURCE_NOT_FOUND, id));
	}
}
