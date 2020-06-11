/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class GetElement extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(GetElement.class);
	
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

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement element = findElement(webDriver, webLocator);
		String elementHTML = getElementHtml(element);

		logInfo("Obtained element: %s.", elementHTML);

		String id = params.get(FindElement.PARAM_ID);
		if (id == null || id.isEmpty())
			id = "";
		else
		{
			context.getContextData().put(id, element.getText());
			id += "=";
		}

		return id + elementHTML;
	}
	
	protected String getElementHtml(WebElement element)
	{
		return element.getAttribute("outerHTML");
	}
}
