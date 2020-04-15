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
import com.exactprosystems.remotehand.ScriptExecuteException;
import org.openqa.selenium.Alert;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;

import static java.lang.String.format;

/**
 * Created by alexey.karpukhin on 9/18/17.
 */
public class WebUtils {
	
	public static File createDownloadDirectory() {
		WebConfiguration configuration = (WebConfiguration) Configuration.getInstance();
		File downloadsDir = configuration.getDownloadsDir();
		if (!configuration.isCreateDownloadSubDir())
		{
			downloadsDir.mkdirs();
			return downloadsDir;
		}
		File newFileDir = new File(downloadsDir, String.valueOf(System.currentTimeMillis()));
		newFileDir.mkdirs();
		return newFileDir;
	}

	public static void deleteDownloadDirectory(File downloadDir)
	{
		if (!((WebConfiguration) Configuration.getInstance()).isCreateDownloadSubDir())
			return;
		
		File[] tmp;
		if (downloadDir != null && (tmp = downloadDir.listFiles()) != null && tmp.length == 0)
			downloadDir.delete();
	}
	
	public static Alert waitForAlert(WebDriver webDriver, int timeoutSec) throws ScriptExecuteException
	{
		try
		{
			WebDriverWait wait = new WebDriverWait(webDriver, timeoutSec);
			return wait.until(ExpectedConditions.alertIsPresent());
		}
		catch (TimeoutException e)
		{
			throw new ScriptExecuteException(format("Timed out after %s seconds waiting for alert.", timeoutSec), e);
		}
		catch (WebDriverException e)
		{
			throw new ScriptExecuteException("Error while waiting for alert.", e);
		}
	}
}
