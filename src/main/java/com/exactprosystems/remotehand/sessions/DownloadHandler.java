/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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