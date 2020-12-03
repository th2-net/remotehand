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

import com.exactprosystems.remotehand.ScriptExecuteException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebJsUtils
{
	private static final Logger logger = LoggerFactory.getLogger(WebJsUtils.class);


	public static void executeJsCommands(WebDriver webDriver, Collection<String> commands) throws ScriptExecuteException
	{
		if (!(webDriver instanceof JavascriptExecutor))
			throw new ScriptExecuteException("Web driver is not JavaScript executor: JS commands cannot be executed");
		
		JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
		for (String command : commands)
		{
			executeJsCommand(jsExecutor, command);
		}
	}

	private static void executeJsCommand(JavascriptExecutor jsExecutor, String javaScript) throws ScriptExecuteException
	{
		try
		{
			logger.info("Executing JS command: {}", javaScript);
			Object res = jsExecutor.executeScript(javaScript);
			if (res != null)
				logger.info("Result of JS command: {} = {}", javaScript, res);
		}
		catch (Exception e)
		{
			throw new ScriptExecuteException("JS command cannot be executed: " + javaScript, e);
		}
	}
}