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

import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class WaitForNew extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(WaitForNew.class);
	private static final String PARAM_SECONDS = "seconds", 
			PARAM_CHECKMILLIS = "checkmillis";
	
	public WaitForNew()
	{
		super.mandatoryParams = new String[]{PARAM_SECONDS, PARAM_CHECKMILLIS};
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

	@Override
	protected Logger getLogger()
	{
		return logger;
	}

	@Override
	public String run(WebDriver webDriver, final By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		final int seconds = getIntegerParam(params, PARAM_SECONDS), 
				checkMillis = getIntegerParam(params, PARAM_CHECKMILLIS);

		try
		{
			(new WebDriverWait(webDriver, seconds)).until(new ExpectedCondition<Boolean>()
			{
				List<WebElement> previusElements = null;

				@Override
				public Boolean apply(WebDriver driver)
				{
					List<WebElement> elements = driver.findElements(webLocator);

					boolean foundEquals = false;
					if (previusElements != null)
					{
						foundEquals = elements.equals(previusElements);

						if (!foundEquals)
							try
							{
								Thread.sleep(checkMillis);
							}
							catch (InterruptedException e)
							{
								// do nothing
							}
					}

					previusElements = elements;

					return foundEquals;
				}
			});

			logInfo("Appeared locator: '%s'.", webLocator);
		}
		catch (TimeoutException ex)
		{
			throw new ScriptExecuteException("Timed out after " + seconds + " seconds waiting for '" + webLocator.toString() + "'");
		}

		return null;
	}
}
