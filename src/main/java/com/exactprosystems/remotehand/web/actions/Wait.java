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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class Wait extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(Wait.class);
	private static final String PARAM_SECONDS = "seconds";
	
	public Wait()
	{
		super.mandatoryParams = new String[]{PARAM_SECONDS};
	}

	@Override
	public boolean isNeedLocator()
	{
		return false;
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
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		int secs = getIntegerParam(params, PARAM_SECONDS);
		logInfo("Pause for "+secs+" second(s)");
		webWait(webDriver, secs);

		return null;
	}
	
	public static void webWait(WebDriver webDriver, int seconds)
	{
		try
		{
			(new WebDriverWait(webDriver, seconds)).until((new ExpectedCondition<Boolean>()
			{
				@Override
				public Boolean apply(WebDriver driver)
				{
					return false;
				}
			}));
		}
		catch (TimeoutException ex)
		{
			// Nothing should happen, it's normal to have timeout here
		}
	}
}
