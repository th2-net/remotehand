////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web.actions;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class FindElement extends WebAction
{
	private static final Logger logger = Logger.getLogger(FindElement.class);
	
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
			waitForElement(webDriver, waitDuration, webLocator);
			result = RESULT_FOUND;
		}
		catch (ScriptExecuteException e)
		{
			result = RESULT_NOTFOUND;
		}
		return id+result;
	}
}