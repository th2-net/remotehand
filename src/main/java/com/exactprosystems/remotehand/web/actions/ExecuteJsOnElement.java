/******************************************************************************
 * Copyright (c) 2009-2021, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.remotehand.web.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ExecuteJsOnElement extends ExecuteJS
{
	private static final Logger logger = LoggerFactory.getLogger(ExecuteJsOnElement.class);

	protected static final String ELEMENT_PARAM = "%Element%";
	protected static final String ARGUMENT = "arguments[0]";

	@Override
	protected String getJsScript(Map<String, String> params)
	{
		return params.get(JS_COMMANDS).replace(ELEMENT_PARAM, ARGUMENT);
	}

	@Override
	protected Object[] getJsArguments(WebDriver webDriver, By webLocator, Map<String, String> params)
	{
		WebElement element = findElement(webDriver, webLocator);
		return new Object[] { element };
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
