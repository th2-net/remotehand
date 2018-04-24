/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.Map;

/**
 * Created by victor.akimov on 3/14/17.
 */
public class UploadFile  extends WebAction
{
	private static final Logger logger = Logger.getLogger(UploadFile.class);
	private static final String ABSOLUT_PATH = "absolutepath";

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		String absolutePath = params.get(ABSOLUT_PATH);
		File file = new File(absolutePath);
		
		if(!file.exists())
			throw new ScriptExecuteException("File " + absolutePath + " is not exist");

		WebElement browseButton = webDriver.findElement(webLocator);
		browseButton.sendKeys(file.getAbsolutePath());
		return null;
	}

	@Override
	public boolean isNeedLocator()
	{
		return true;
	}

	@Override
	public boolean isCanWait()
	{
		return true;
	}

	@Override
	protected Logger getLogger()
	{
		return logger;
	}
}
