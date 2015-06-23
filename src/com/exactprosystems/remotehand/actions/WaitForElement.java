////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.actions;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exactprosystems.remotehand.Logger;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;

public class WaitForElement extends WebAction
{
	private static final Logger logger = Logger.getLogger();
	private static final String PARAM_SECONDS = "seconds";

	public WaitForElement()
	{
		super.mandatoryParams = new String[]{PARAM_SECONDS};
	}
	
	@Override
	public boolean isNeedLocator()
	{
		return true;
	}
	
	@Override
	public boolean isCanWait()
	{
		return false;
	}
	
	public static void waitForElement(final By webLocator, WebDriver webDriver, int seconds) throws ScriptExecuteException
	{
		try
		{
			(new WebDriverWait(webDriver, seconds)).until(new ExpectedCondition<Boolean>()
			{
				@Override
				public Boolean apply(WebDriver driver)
				{
					List<WebElement> elements = driver.findElements(webLocator);

					return elements.size() > 0;
				}
			});

			logger.info("Appeared locator: '" + webLocator.toString() + "'");
		}
		catch (TimeoutException ex)
		{
			throw new ScriptExecuteException("Timed out after " + seconds + " seconds waiting for '" + webLocator.toString() + "'");
		}
	}
	

	@Override
	public String run(WebDriver webDriver, final By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		int seconds = getIntegerParam(params, PARAM_SECONDS);
		waitForElement(webLocator, webDriver, seconds);

		return null;
	}
}
