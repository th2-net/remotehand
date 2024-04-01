/*
 * Copyright 2020-2024 Exactpro (Exactpro Systems Limited)
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
import com.exactpro.remotehand.ActionResult;
import com.exactpro.remotehand.ScriptCompileException;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.utils.RhUtils;
import com.exactpro.remotehand.web.webelements.WebLocator;
import com.exactpro.remotehand.windows.SessionLogger;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.exactpro.remotehand.utils.RhUtils.isBrowserNotReachable;

public abstract class WebAction extends Action {
	protected static final String PARAM_WAIT = "wait";
	protected static final String PARAM_NOT_FOUND_FAIL = "notfoundfail";

	protected final boolean locatorNeeded;
	protected final boolean canWait;
	protected final String[] mandatoryParams;
	protected boolean canSwitchPage = false;
	protected WebSessionContext context;
	protected Logger logger;
	private WebLocator webLocator = null;
	private Map<String, String> params = null;

	public WebAction(boolean locatorNeeded, boolean canWait, String... mandatoryParams) {
		this.locatorNeeded = locatorNeeded;
		this.canWait = canWait;
		this.mandatoryParams = mandatoryParams;
	}

	public void init(WebSessionContext context, WebLocator webLocator, Map<String, String> params) throws ScriptCompileException {
		this.context = context;
		this.webLocator = webLocator;
		this.params = params;
		this.logger = new SessionLogger(context.getSessionId(), LoggerFactory.getLogger(this.getClass()));
	}

	public static int getIntegerParam(Map<String, String> params, String paramName) throws ScriptExecuteException {
		try {
			return Integer.parseInt(params.get(paramName));
		} catch (NumberFormatException e) {
			throw new ScriptExecuteException(String.format("Error while parsing parameter '%s' = '%s' as number",
					paramName, params.get(paramName)), e);
		}
	}

	public static int getIntegerParam(Map<String, String> params, String paramName, int defaultValue) {
		try {
			return Integer.parseInt(params.get(paramName));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public boolean isLocatorNeeded() {
		return locatorNeeded;
	}

	public boolean isCanWait() {
		return canWait;
	}

	public abstract String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException;

	public boolean isCanSwitchPage() {
		return canSwitchPage;
	}

	public boolean isElementMandatory() {
		return !params.containsKey(PARAM_NOT_FOUND_FAIL) || RhUtils.YES.contains(params.get(PARAM_NOT_FOUND_FAIL));
	}

	protected boolean waitForElement(WebDriver webDriver, int seconds, By webLocator) throws ScriptExecuteException {
		return waitForElement(webDriver, seconds, webLocator, isElementMandatory());
	}

	protected boolean waitForElement(
			WebDriver webDriver,
			int seconds,
			By webLocator,
			boolean isElementMandatory
	) throws ScriptExecuteException {
		try {
			new WebDriverWait(webDriver, seconds).until((ExpectedCondition<Boolean>) webDriver1 ->
					webDriver1 != null && !webDriver1.findElements(webLocator).isEmpty()
			);
			if (logger.isInfoEnabled()) {
				logger.info("Appeared locator: '{}'", webLocator);
			}
		} catch (TimeoutException ex) {
			if (isElementMandatory)
				throw new ScriptExecuteException(String.format("Timed out after %s seconds waiting for '%s'",
						seconds, webLocator), ex);
			return false;
		}
		return true;
	}

	@Override
	public void beforeExecute() {
		if (context == null || context.getContextData() == null || context.getContextData().isEmpty()) {
			if (logger.isWarnEnabled()) {
				if (context == null)
					logger.warn("Context is null");
				else if (context.getContextData() == null)
					logger.warn("ContextData is null");
			}
			return;
		}

		for (Map.Entry<String, String> param : params.entrySet()) {
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
	public ActionResult execute() throws ScriptExecuteException {
		try {
			WebDriver webDriver = context.getWebDriver();

			By locator = null;
			if (webLocator != null)
				locator = webLocator.getWebLocator(webDriver, params);

			boolean needRun = true;
			if (isCanWait()) {
				int waitSecs = getIntegerParam(params, PARAM_WAIT, 0);
				if (waitSecs != 0 && !waitForElement(webDriver, waitSecs, locator))
					needRun = false;
			}

			if (isCanSwitchPage() && isNeedDisableLeavePageAlert())
				disableLeavePageAlert(webDriver);

			return needRun ? buildResult(run(webDriver, locator, params)) : null;
		} catch (ScriptExecuteException e) {
			throw addScreenshot(e);
		} catch (WebDriverException e) {
			ScriptExecuteException see = new ScriptExecuteException(e.getMessage(), e);
			throw isBrowserNotReachable(e) ? see : addScreenshot(see);
		}
	}

	protected ScriptExecuteException addScreenshot(ScriptExecuteException see) {
		if (WebConfiguration.getInstance().isScreenshotOnError()) {
			String screenshotId = null;
			try {
				screenshotId = takeAndSaveScreenshot(null);
			} catch (ScriptExecuteException e) {
				logger.error("Could not create screenshot", e);
			}
			see.setScreenshotId(screenshotId);
		}
		return see;
	}

	public String[] getMandatoryParams() throws ScriptCompileException {
		return mandatoryParams;
	}

	public boolean isNeedDisableLeavePageAlert() {
		return WebConfiguration.getInstance().isDisableLeavePageAlert();
	}

	public void disableLeavePageAlert(WebDriver webDriver) {
		((JavascriptExecutor)webDriver).executeScript("window.onbeforeunload = function(e){};");
	}

	public WebLocator getWebLocator() {
		return webLocator;
	}

	public Map<String, String> getParams() {
		return params;
	}

	protected WebElement findElement(WebDriver webDriver, By webLocator) {
		WebElement element = webDriver.findElement(webLocator);
		if (!element.isDisplayed())
			scrollTo(element, webLocator);
		return element;
	}

	protected void scrollTo(WebElement element, By webLocator) {
		if (element instanceof Locatable) {
			((Locatable)element).getCoordinates().inViewPort();
			logger.info("Scrolled to {}.", webLocator);
		} else {
			logger.info("Cannot scroll {}.", webLocator);
		}
	}

	protected String takeAndSaveScreenshot(String name) throws ScriptExecuteException {
		WebDriver webDriver = context.getWebDriver();
		if (!(webDriver instanceof TakesScreenshot))
			throw new ScriptExecuteException("Current driver doesn't support taking screenshots.");
		return screenWriter.takeAndSaveScreenshot(name, (TakesScreenshot) webDriver);
	}

	protected int getChromeDriverVersion(ChromeDriver chromeDriver) {
		Map<?, ?> chromeCap = (Map<?, ?>) chromeDriver.getCapabilities().getCapability("chrome");
		String ver = (String) chromeCap.get("chromedriverVersion");
		Matcher m = Pattern.compile("^\\d*").matcher(ver);
		return m.find() ? Integer.parseInt(m.group()) : -1;
	}
}