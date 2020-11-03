/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web;

import java.util.Collection;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebJsUtils
{
	private static final Logger logger = LoggerFactory.getLogger(WebJsUtils.class);


	public static void executeJsCommands(WebDriver webDriver, Collection<String> commands)
	{
		for (String script : commands)
		{
			executeJsCommand(webDriver, script);
		}
	}

	public static void executeJsCommand(WebDriver webDriver, String javaScript)
	{
		try
		{
			if (webDriver instanceof JavascriptExecutor)
				((JavascriptExecutor) webDriver).executeScript(javaScript);
			else
				logger.error("Web driver is not Javascript executor. Javascript cannot be executed");
		}
		catch (Exception e)
		{
			logger.error("Framework Error: Unable to Execute Java Script: " + javaScript + "with error: ", e);
		}
	}
}