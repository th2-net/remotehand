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

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.web.WebAction;

public class Open extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(Open.class);
	private static final String PARAM_URL = "url";
	
	public Open()
	{
		super.mandatoryParams = new String[]{PARAM_URL};
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
	public boolean isCanSwitchPage()
	{
		return true;
	}

	@Override
	protected Logger getLogger()
	{
		return logger;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params)
	{
		final String url = params.get(PARAM_URL);

		webDriver.get(url);
		webDriver.manage().window().maximize();

		logInfo("Opened: '%s'", url);

		return null;
	}
}
