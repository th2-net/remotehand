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

package com.exactprosystems.remotehand.web;

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
		WebConfiguration configuration = WebConfiguration.getInstance();
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
		if (!WebConfiguration.getInstance().isCreateDownloadSubDir())
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
