////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.exactprosystems.remotehand.actions.WaitForElement;

public abstract class WebAction
{
	protected static final String PARAM_WAIT = "wait";
	
	protected String[] mandatoryParams;
	
	
	public static int getIntegerParam(Map<String, String> params, String paramName) throws ScriptExecuteException
	{
		try
		{
			return Integer.parseInt(params.get(paramName));
		}
		catch (NumberFormatException ex)
		{
			throw new ScriptExecuteException("Error while parsing parameter '" + paramName + "' = '" + params.get(paramName) + "' as number");
		}
	}
	
	public abstract boolean isNeedLocator();
	public abstract boolean isCanWait();
	public abstract String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException;
	
	
	public String execute(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		if (isCanWait())
		{
			if ((params.containsKey(PARAM_WAIT)) && (!params.get(PARAM_WAIT).isEmpty()))
				WaitForElement.waitForElement(webLocator, webDriver, getIntegerParam(params, PARAM_WAIT));
		}
		
		return run(webDriver, webLocator, params);
	}

	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return mandatoryParams;
	};
}
