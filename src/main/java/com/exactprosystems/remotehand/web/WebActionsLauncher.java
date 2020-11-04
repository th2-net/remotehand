/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.rhdata.RhScriptResult;
import com.exactprosystems.remotehand.*;
import com.exactprosystems.remotehand.sessions.SessionContext;

import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.exactprosystems.remotehand.RhUtils.isBrowserNotReachable;

public class WebActionsLauncher extends ActionsLauncher
{
	private static final Logger logger = LoggerFactory.getLogger(WebActionsLauncher.class);
	
	public WebActionsLauncher(ScriptProcessorThread parentThread)
	{
		super(parentThread);
	}

	@Override
	protected void beforeActions(SessionContext context) throws ScriptExecuteException, RhConfigurationException
	{
		super.beforeActions(context);
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
		WebDriverWrapper webDriver = context.getWebDriverWrapper();
		try
		{
			webDriver.getDriver().getCurrentUrl();
		}
		catch (WebDriverException e)
		{
			logger.warn("Error received as result of checking browser state", e);
			closeOldDriver(webDriver, context);
			if (isBrowserNotReachable(e))
				updateWebDriver(context);
			else
				throw new ScriptExecuteException("Error while checking browser state", e);
		}
	}

	private void closeOldDriver(WebDriverWrapper driver, WebSessionContext context)
	{
		WebDriverManager manager = context.getWebDriverManager();
		manager.closeWebDriver(driver, context.getSessionId());
	}

	private void updateWebDriver(WebSessionContext context) throws ScriptExecuteException, RhConfigurationException
	{
		logger.info("Trying to create new driver...");
		WebDriverManager driverManager = context.getWebDriverManager();
		driverManager.createWebDriver(context);
	}
}
