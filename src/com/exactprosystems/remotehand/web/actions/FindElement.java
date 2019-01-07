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

public class FindElement extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(FindElement.class);
	
	public static final String PARAM_ID = "id",
			RESULT_FOUND = "found",
			RESULT_NOTFOUND = "notfound";
	
	@Override
	public boolean isNeedLocator()
	{
		return true;
	}
	
	@Override
	public boolean isCanWait()
	{
		return false;  //Action implements the waiting logic by itself
	}

	@Override
	protected Logger getLogger()
	{
		return logger;
	}
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		int waitDuration;
		if ((params.containsKey(PARAM_WAIT)) && (!params.get(PARAM_WAIT).isEmpty()))
			waitDuration = getIntegerParam(params, PARAM_WAIT);
		else
			waitDuration = 0;
		
		String id = params.get(PARAM_ID);
		if (id == null)
			id = "";
		else if (!id.isEmpty())
			id += "=";
		
		String result;
		try
		{
			boolean found = waitForElement(webDriver, waitDuration, webLocator);
			result = (found) ? RESULT_FOUND : RESULT_NOTFOUND;
		}
		catch (ScriptExecuteException e)
		{
			result = RESULT_NOTFOUND;
		}
		return id+result;
	}
}
