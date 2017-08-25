////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.remotehand.web;

import com.exactprosystems.clearth.connectivity.data.rhdata.RhScriptResult;
import com.exactprosystems.remotehand.*;
import com.exactprosystems.remotehand.http.SessionContext;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import static com.exactprosystems.remotehand.RhUtils.isBrowserNotReachable;

/**
 * @author anna.bykova.
 */
public class WebActionsLauncher extends ActionsLauncher
{
	private static final Logger logger = Logger.getLogger(WebActionsLauncher.class);
	
	public WebActionsLauncher(ScriptProcessorThread parentThread)
	{
		super(parentThread);
	}

	@Override
	protected void beforeActions(SessionContext context) throws ScriptExecuteException, RhConfigurationException
	{
		checkWebDriver((WebSessionContext) context);
	}

	@Override
	protected void processActionResult(RhScriptResult scriptResult, Action action, String actionResult)
	{
		WebAction webAction = (WebAction) action;
		switch (webAction.getOutputType())
		{
			case SCREENSHOT:
				scriptResult.addScreenshotId(actionResult);
				break;
			case ENCODED_DATA:
				scriptResult.addToEncodedOutput(actionResult);
				break;
			default:
				scriptResult.addToTextOutput(actionResult);
		}
	}
	
	private void checkWebDriver(WebSessionContext context) throws ScriptExecuteException, RhConfigurationException
	{
		WebDriver webDriver = context.getWebDriver();
		try
		{
			webDriver.getCurrentUrl();
		}
		catch (WebDriverException e)
		{
			logger.warn("Error received as result of checking browser state", e);
			closeOldDriver(webDriver);
			if (isBrowserNotReachable(e))
				updateWebDriver(context);
			else
				throw new ScriptExecuteException("Error while checking browser state: " + e.getMessage(), e);
		}
	}
	
	private void closeOldDriver(WebDriver driver)
	{
		try
		{
			driver.quit();
		}
		catch (Exception e)
		{
			logger.warn("Error while closing driver referenced to unreachable browser", e);
		}
	}
	
	private void updateWebDriver(WebSessionContext context) throws ScriptExecuteException, RhConfigurationException
	{
		logger.info("Trying to create new driver...");
		WebDriverManager driverManager = context.getWebDriverManager();
		context.setWebDriver(driverManager.createWebDriver());
	}
}
