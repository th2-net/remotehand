////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web;

import java.util.Map;

import com.exactprosystems.remotehand.Action;
import com.exactprosystems.remotehand.ScriptAction;
import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.actions.WaitForElement;
import com.exactprosystems.remotehand.web.webelements.WebLocator;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public abstract class WebAction implements Action
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


	public boolean isCanSwitchPage()
	{
		return false;
	}
	
	public String execute(ScriptAction actionParams) throws ScriptExecuteException
	{
		WebScriptAction webScriptAction = (WebScriptAction) actionParams;
		Map<String, String> params = webScriptAction.getParams();
		WebLocator locator = webScriptAction.getWebLocator();
		WebDriver webDriver = webScriptAction.getDriver();

		By webLocator = null;
		if (locator != null)
		{
			webLocator = locator.getWebLocator(webDriver, params);
		}


		if (isCanWait())
		{
			if ((params.containsKey(PARAM_WAIT)) && (!params.get(PARAM_WAIT).isEmpty()))
				WaitForElement.waitForElement(webLocator, webDriver, getIntegerParam(params, PARAM_WAIT));
		}

		if (isCanSwitchPage())
			disableLeavePageAlert(webDriver);


		
		return run(webDriver, webLocator, params);
	}

	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return mandatoryParams;
	}

	public void disableLeavePageAlert(WebDriver webDriver)
	{
		((JavascriptExecutor)webDriver).executeScript("window.onbeforeunload = function(e){};");
	}
}
