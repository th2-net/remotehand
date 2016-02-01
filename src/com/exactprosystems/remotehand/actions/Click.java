////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.actions;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;

public class Click extends WebAction
{
	private static final Logger logger = Logger.getLogger(Click.class);
	
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
	public boolean isCanSwitchPage()
	{
		return true;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement element = webDriver.findElement(webLocator);
		try {
			element.click();
		} catch (ElementNotVisibleException e) {
			logger.error("Element is not visible" ,e);

			JavascriptExecutor js = (JavascriptExecutor) webDriver;
			js.executeScript("arguments[0].click();", element);

		}

		logger.info("Clicked on: '" + element.toString() + "'");

		return null;
	}
}
