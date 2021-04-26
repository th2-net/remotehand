/*
 * Copyright 2020-2021 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand.web;

import com.exactpro.remotehand.Action;
import com.exactpro.remotehand.RhUtils;
import com.exactpro.remotehand.ScriptCompileException;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.webelements.WebLocator;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.exactpro.remotehand.RhUtils.isBrowserNotReachable;

public abstract class WebAction extends Action
{
	protected static final String PARAM_WAIT = "wait", PARAM_NOTFOUNDFAIL = "notfoundfail";
	
	protected String[] mandatoryParams;

	protected WebSessionContext context;
	private String sessionIdForLogs;
	private WebLocator webLocator = null;
	private Map<String, String> params = null;

	public void init(WebSessionContext context, WebLocator webLocator, Map<String, String> params) throws ScriptCompileException
	{
		this.context = context;
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
		catch (NumberFormatException e)
		{
			throw new ScriptExecuteException(String.format("Error while parsing parameter '%s' = '%s' as number",
					paramName, params.get(paramName)), e);
		}
	}

	public static int getIntegerParam(Map<String, String> params, String paramName, int defaultValue)
	{
		try
		{
			return Integer.parseInt(params.get(paramName));
		}
		catch (NumberFormatException e)
		{
			return defaultValue;
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
		return !params.containsKey(PARAM_NOTFOUNDFAIL) || RhUtils.YES.contains(params.get(PARAM_NOTFOUNDFAIL));
	}
	
	protected boolean waitForElement(WebDriver webDriver, int seconds, By webLocator) throws ScriptExecuteException
	{
		return waitForElement(webDriver, seconds, webLocator, isElementMandatory());
	}
	
	protected boolean waitForElement(WebDriver webDriver, int seconds, By webLocator, boolean isElementMandatory)
			throws ScriptExecuteException
	{
		try
		{
			new WebDriverWait(webDriver, seconds).until((ExpectedCondition<Boolean>) webDriver1 ->
					webDriver1 != null && webDriver1.findElements(webLocator).size() > 0);
			logInfo("Appeared locator: '%s'", webLocator);
		}
		catch (TimeoutException ex)
		{
			if (isElementMandatory)
				throw new ScriptExecuteException(String.format("Timed out after %s seconds waiting for '%s'",
						seconds, webLocator), ex);
			return false;
		}
		return true;
	}
	
	@Override
	public void beforeExecute()
	{
		if (context == null || context.getContextData() == null || context.getContextData().isEmpty())
		{
			if (context == null)
				getLogger().warn("Context is null");
			else if (context.getContextData() == null)
				getLogger().warn("ContextData is null");
			return;
		}

		for (Map.Entry<String, String> param : params.entrySet())
		{
			int start;
			String value = param.getValue();
			if((start = value.indexOf("@{")) < 0)
				continue;

			int end = value.lastIndexOf('}');
			String name = value.substring(start + 2, end);
			String contextValue = (String) context.getContextData().get(name);

			if(StringUtils.isNotEmpty(contextValue))
				value = value.substring(0, start) + contextValue + value.substring(end + 1);

			param.setValue(value);
		}
	}

	@Override
	public String execute() throws ScriptExecuteException
	{
		try
		{
			WebDriver webDriver = context.getWebDriver();
			
			By locator = null;
			if (webLocator != null)
				locator = webLocator.getWebLocator(webDriver, params);

			boolean needRun = true;
			if (isCanWait())
			{
				int waitSecs = getIntegerParam(params, PARAM_WAIT, 0);
				if (waitSecs != 0 && !waitForElement(webDriver, waitSecs, locator))
					needRun = false;
			}

			if (isCanSwitchPage() && isNeedDisableLeavePageAlert())
				disableLeavePageAlert(webDriver);

			return (needRun) ? run(webDriver, locator, params) : null;
		}
		catch (ScriptExecuteException e)
		{
			throw addScreenshot(e);
		}
		catch (WebDriverException e)
		{
			ScriptExecuteException see = new ScriptExecuteException(e.getMessage(), e);
			if (isBrowserNotReachable(e))
				throw see;
			throw addScreenshot(see);
		}
	}
	
	protected ScriptExecuteException addScreenshot(ScriptExecuteException see)
	{
		String screenshotId = null;
		try {
			screenshotId = takeAndSaveScreenshot(null);
		} catch (ScriptExecuteException e) {
			logError("Could not create screenshot", e);
		}
		see.setScreenshotId(screenshotId);
		return see;
	}

	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return mandatoryParams;
	}
	
	public boolean isNeedDisableLeavePageAlert()
	{
		return WebConfiguration.getInstance().isDisableLeavePageAlert();
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

	protected String takeAndSaveScreenshot(String name) throws ScriptExecuteException
	{
		WebDriver webDriver = context.getWebDriver();
		if (!(webDriver instanceof TakesScreenshot))
			throw new ScriptExecuteException("Current driver doesn't support taking screenshots.");
		return screenWriter.takeAndSaveScreenshot(name, (TakesScreenshot) webDriver);
	}

	protected int getChromeDriverVersion(ChromeDriver chromeDriver)
	{
		Map<?, ?> chromeCap = (Map<?, ?>) chromeDriver.getCapabilities().getCapability("chrome");
		String ver = (String) chromeCap.get("chromedriverVersion");
		Matcher m = Pattern.compile("^\\d*").matcher(ver);
		if (m.find())
			return Integer.parseInt(m.group());
		else
			return -1;
	}
	
	
	protected void logError(String msg)
	{
		getLogger().error("{}{}", sessionIdForLogs, msg);
	}
	
	protected void logError(String msg, Throwable e)
	{
		getLogger().error(String.format("%s%s", sessionIdForLogs, msg), e);
	}
	
	protected void logWarn(String msg)
	{
		getLogger().warn(msg);
	}
	
	protected void logWarn(String msgTemplate, Object... args)
	{
		getLogger().warn("{}{}", sessionIdForLogs, String.format(msgTemplate, args));
	}
	
	protected void logInfo(String msg)
	{
		Logger logger = getLogger();
		if (logger.isInfoEnabled())
			logger.info("{}{}", sessionIdForLogs, msg);
	}
	
	protected void logInfo(String msgTemplate, Object... args)
	{
		Logger logger = getLogger();
		if (logger.isInfoEnabled())
			logger.info("{}{}", sessionIdForLogs, String.format(msgTemplate, args));
	}
}
