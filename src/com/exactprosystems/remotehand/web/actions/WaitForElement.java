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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class WaitForElement extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(WaitForElement.class);
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

	@Override
	protected Logger getLogger()
	{
		return logger;
	}
	
	@Override
	public String run(WebDriver webDriver, final By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		int seconds = getIntegerParam(params, PARAM_SECONDS);
		waitForElement(webDriver, seconds, webLocator);
		return null;
	}
}
