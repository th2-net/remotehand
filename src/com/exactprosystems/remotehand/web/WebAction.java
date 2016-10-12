////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web;

import java.util.List;
import java.util.Map;

import com.exactprosystems.remotehand.Action;
import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.webelements.WebLocator;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import static java.lang.String.format;

public abstract class WebAction extends Action
{
	protected static final String PARAM_WAIT = "wait",
			PARAM_NOTFOUNDFAIL = "notfoundfail";
	
	protected String[] mandatoryParams;

	private WebDriver webDriver = null;
	private String sessionIdForLogs;
	private WebLocator webLocator = null;
	private Map<String, String> params = null;

	public void init(WebSessionContext context, WebLocator webLocator, Map<String, String> params) throws ScriptCompileException
	{
		this.webDriver = context.getWebDriver();
		this.sessionIdForLogs = '<' + context.getSessionId() + "> ";
		this.webLocator = webLocator;
		this.params = params;
	}
	
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
	protected abstract Logger getLogger();


	public boolean isCanSwitchPage()
	{
		return false;
	}
	
	public boolean isElementMandatory()
	{
		return !params.containsKey(PARAM_NOTFOUNDFAIL) || WebScriptCompiler.YES.contains(params.get(PARAM_NOTFOUNDFAIL));
	}
	
	protected boolean waitForElement(WebDriver webDriver, int seconds, final By webLocator) throws ScriptExecuteException
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
			logInfo("Appeared locator: '%s'", webLocator);
		}
		catch (TimeoutException ex)
		{
			if (isElementMandatory())
				throw new ScriptExecuteException("Timed out after " + seconds + " seconds waiting for '" + webLocator.toString() + "'");
			else
				return false;
		}
		return true;
	}
	
	@Override
	public String execute() throws ScriptExecuteException
	{
		By locator = null;
		if (webLocator != null)
			locator = webLocator.getWebLocator(webDriver, params);
		
		boolean needRun = true;
		if (isCanWait())
		{
			if ((params.containsKey(PARAM_WAIT)) && (!params.get(PARAM_WAIT).isEmpty()))
				if (!waitForElement(webDriver, getIntegerParam(params, PARAM_WAIT), locator))
					needRun = false;
		}

		if (isCanSwitchPage())
			disableLeavePageAlert(webDriver);


		if (needRun)
			return run(webDriver, locator, params);
		else
			return null;
	}

	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return mandatoryParams;
	}

	public void disableLeavePageAlert(WebDriver webDriver)
	{
		((JavascriptExecutor)webDriver).executeScript("window.onbeforeunload = function(e){};");
	}


	public WebLocator getWebLocator() {
		return webLocator;
	}


	public Map<String, String> getParams() {
		return params;
	}
	
	protected WebElement findElement(WebDriver webDriver, By webLocator)
	{
		WebElement element = webDriver.findElement(webLocator);
		if (!element.isDisplayed())
			scrollTo(element, webLocator);
		return element;
	}
	
	protected void scrollTo(WebElement element, By webLocator)
	{
		if (element instanceof Locatable)
		{
			((Locatable)element).getCoordinates().inViewPort();
			logInfo("Scrolled to %s.", webLocator);
		}
		else 
			logWarn("Cannot scroll %s.", webLocator);
	}
	
	protected void logError(String msg)
	{
		getLogger().error(sessionIdForLogs + msg);
	}
	
	protected void logError(String msg, Throwable e)
	{
		getLogger().error(sessionIdForLogs + msg, e);
	}
	
	protected void logWarn(String msg)
	{
		getLogger().warn(msg);
	}
	
	protected void logWarn(String msgTemplate, Object... args)
	{
		getLogger().warn(sessionIdForLogs + format(msgTemplate, args));
	}
	
	protected void logInfo(String msg)
	{
		Logger logger = getLogger();
		if (logger.isInfoEnabled())
			logger.info(sessionIdForLogs + msg);
	}
	
	protected void logInfo(String msgTemplate, Object... args)
	{
		Logger logger = getLogger();
		if (logger.isInfoEnabled())
			logger.info(sessionIdForLogs + format(msgTemplate, args));
	}
}
