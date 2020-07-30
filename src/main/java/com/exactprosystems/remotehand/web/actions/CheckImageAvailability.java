/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *  
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web.actions;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class CheckImageAvailability extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(CheckImageAvailability.class);

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
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement image = findElement(webDriver, webLocator);
		Object result = ((JavascriptExecutor) webDriver).executeScript(
				"return arguments[0].complete && " +
						"typeof arguments[0].naturalWidth != \"undefined\" && " +
						"arguments[0].naturalWidth > 0", image);

		boolean loaded = false;
		if (result instanceof Boolean)
		{
			loaded = (Boolean) result;
		}
		return String.valueOf(loaded);
	}

	@Override
	protected Logger getLogger()
	{
		return logger;
	}
}
