////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.actions;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.exactprosystems.remotehand.WebAction;

public class Open extends WebAction
{
	private static final Logger logger = Logger.getLogger(Open.class);
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
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params)
	{
		final String url = params.get(PARAM_URL);

		webDriver.get(url);
		webDriver.manage().window().maximize();

		logger.info("Opened: '" + url + "'");

		return null;
	}
}
