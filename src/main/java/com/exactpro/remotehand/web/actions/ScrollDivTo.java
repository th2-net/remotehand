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

package com.exactpro.remotehand.web.actions;

import com.exactpro.remotehand.ScriptCompileException;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebScriptCompiler;
import com.exactpro.remotehand.web.webelements.WebLocatorsMapping;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ScrollDivTo extends ScrollTo
{
	private static final Logger logger = LoggerFactory.getLogger(ScrollDivTo.class);

	public static final String PARAM_WAIT2 = String.format("%s2", PARAM_WAIT),
			PARAM_LOCATOR2 = String.format("%s2", WebScriptCompiler.WEB_LOCATOR),
			PARAM_MATCHER2 = String.format("%s2", WebScriptCompiler.WEB_MATCHER),
			PARAM_Y_OFFSET = "yoffset";

	protected static final int DEFAULT_Y_OFFSET = 0;

	protected JavascriptExecutor jsExecutor;
	protected WebElement divWithScrollbar;

	@Override
	protected Logger getLogger()
	{
		return logger;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		this.jsExecutor = (JavascriptExecutor) webDriver;
		this.divWithScrollbar = webDriver.findElement(webLocator);

		By webLocator2 = getWebLocator2(webDriver, params);
		if (webLocator2 == null)
			throw new ScriptExecuteException("Element to scroll to didn't appear");

		return doScrollTo(webDriver, webLocator, webLocator2, params);
	}

	protected By getWebLocator2(WebDriver webDriver, Map<String, String> params) throws ScriptExecuteException
	{
		int wait2 = Integer.parseInt(params.getOrDefault(PARAM_WAIT2, "0"));
		String locator2 = params.get(PARAM_LOCATOR2), matcher2 = params.get(PARAM_MATCHER2);
		logInfo("Waiting for element by %s: '%s'", locator2, matcher2);
		try
		{
			By webLocator2 = WebLocatorsMapping.getByName(locator2).getWebLocator(webDriver, matcher2);
			if (!waitForElement(webDriver, wait2, webLocator2, false))
				return null;
			return webLocator2;
		}
		catch (ScriptCompileException e)
		{
			throw new ScriptExecuteException("Error while resolving locator2", e);
		}
	}

	protected String doScrollTo(WebDriver webDriver, By webLocator, By webLocator2, Map<String, String> params)
	{
		WebElement elementToScrollTo = webDriver.findElement(webLocator2);

		int yOffset = getIntegerParam(params, PARAM_Y_OFFSET, DEFAULT_Y_OFFSET);
		scrollDivToElement(divWithScrollbar, elementToScrollTo);
		scrollDivByOffset(divWithScrollbar, yOffset);
		logInfo("Element '%s' located on element '%s' with y-offset %s", webLocator, webLocator2, yOffset);

		return null;
	}
	
	protected void scrollDivToElement(WebElement divWithScrollbar, WebElement elementToScrollTo)
	{
		executeJsScript("arguments[0].scrollTop=arguments[1].offsetTop", divWithScrollbar, elementToScrollTo);
	}
	
	protected void scrollDivByOffset(WebElement divWithScrollbar, int yOffset)
	{
		if (yOffset != 0)
			executeJsScript("arguments[0].scrollTop+=(arguments[1])", divWithScrollbar, yOffset);
	}
	
	protected Object executeJsScript(String script, Object... args)
	{
		return jsExecutor.executeScript(script, args);
	}
}
