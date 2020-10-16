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

import com.exactprosystems.remotehand.*;
import com.exactprosystems.remotehand.web.WebAction;
import com.exactprosystems.remotehand.web.WebScriptCompiler;
import com.exactprosystems.remotehand.web.utils.SendKeysHandler;
import com.exactprosystems.remotehand.web.webelements.WebLocatorsMapping;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendKeys extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(SendKeys.class);

	private static final Pattern HOLD_KEYS_PATTERN = Pattern.compile("\\(#(shift|ctrl|alt)#\\)");
	
	private static final String PARAM_TEXT = "text",
			PARAM_TEXT2 = String.format("%s2", PARAM_TEXT),
			PARAM_WAIT2 = String.format("%s2", PARAM_WAIT),
			PARAM_LOCATOR2 = String.format("%s2", WebScriptCompiler.WEB_LOCATOR),
			PARAM_MATCHER2 = String.format("%s2", WebScriptCompiler.WEB_MATCHER),
			PARAM_CHECKINPUT = "checkinput",
			PARAM_NEEDCLICK = "needclick",
			CLEAR_BEFORE = "clear",
			CAN_BE_DISABLED = "canbedisabled";

	private static final int MAX_RETRIES = Configuration.getInstance().getSendKeysMaxRetries();
	
	private boolean holdShift, holdCtrl, holdAlt;
	private final SendKeysHandler handler = new SendKeysHandler();

	public SendKeys()
	{
		super.mandatoryParams = new String[]{PARAM_TEXT};
	}
	
	@Override
	public boolean isNeedLocator()
	{
		return true;
	}
	
	@Override
	public boolean isCanWait()
	{
		return true;
	}
	
	@Override
	public boolean isCanSwitchPage()
	{
		return true;
	}
	
	@Override
	protected Logger getLogger()
	{
		return logger;
	}
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement input = webLocator != null ? findElement(webDriver, webLocator) : webDriver.switchTo().activeElement();
		if (webLocator == null)
			logInfo("Active element: %s" , input != null ? input.getTagName() : "null");
		if (input == null)
			throw new ScriptExecuteException("Unable to send keys: input element is null");

		boolean shouldBeEnabled = needEnable(input, params);
		try
		{
			if (shouldBeEnabled)
				enable(webDriver, input);

			if (RhUtils.getBooleanOrDefault(params, CLEAR_BEFORE, false))
			{
				input.clear();
				logInfo("Text field has been cleared.");
			}

			boolean checkInput = RhUtils.getBooleanOrDefault(params, PARAM_CHECKINPUT, true);
			boolean needClick = RhUtils.getBooleanOrDefault(params, PARAM_NEEDCLICK, true);
			String text = replaceConversions(checkHoldKeys(params.get(PARAM_TEXT)));
			logInfo("Sending text1 (%s) to locator: %s", text, webLocator);
			sendText(input, text, webDriver, webLocator, 0, checkInput, needClick);
			logInfo("Text '%s' was sent to locator: %s.", text, webLocator);

			String text2 = replaceConversions(params.get(PARAM_TEXT2));
			if (StringUtils.isNotEmpty(text2) && needRun(webDriver, params))
			{
				logInfo("Sending text2 to: %s", webLocator);
				sendText(input, text2, webDriver, webLocator, 0, checkInput, needClick);
				logInfo("Sent text2 to: %s", webLocator);
			}
		}
		finally
		{
			if (shouldBeEnabled)
				disable(webDriver, input);
		}
		return null;
	}
	
	protected void sendText(WebElement input, String text, WebDriver driver, By locator, int retries,
			boolean checkInput, boolean needClick) throws ScriptExecuteException
	{
		
		List<String> strings = handler.processInputText(text);
		
		if (retries > 0)
		{
			logInfo("Trying to scroll input element into view...");
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);
		}
		Actions actions = new Actions(driver);
		if (needClick)
		{
			doClick(actions, input);
			// find element again to avoid 'cannot focus element' error
			input = locator != null ? findElement(driver, locator) : driver.switchTo().activeElement();
		}
		doHoldKeys(driver);
		
		for (String str : strings)
		{
			if (str.startsWith(SendKeysHandler.KEY_SIGN))
			{
				handler.sendSpecialKey(actions, str, locator);
				continue;
			}

			String inputAtStart = input.getAttribute("value");
			handler.doSendKeys(actions, str);
			if (inputAtStart == null)
			{
				logWarn("Input field does not contain value attribute. Sending text as is.");
				continue;
			}

			if (checkInput && driver instanceof ChromeDriver)
				checkInput(input, text, driver, locator, retries, needClick, str, inputAtStart);
		}
		doReleaseKeys(driver);
	}
	
	protected void checkInput(WebElement input, String text, WebDriver driver, By locator, int retries,
			boolean needClick, String str, String inputAtStart) throws ScriptExecuteException
	{
		String result = input.getAttribute("value");
		boolean equals = result.equals(str);
		if (!equals && result.startsWith(inputAtStart))
			equals = result.replaceFirst(Pattern.quote(inputAtStart), "").equals(str);
		if (!equals)
		{
			if (retries >= MAX_RETRIES)
			{
				logWarn("Missed input detected, but too many retries were already done.");
				logWarn("Unable to send text '{}' to locator '{}'", text, locator);
				return;
			}

			// If field not filled as expected for current moment, restart operation at all
			logInfo("Missed input detected. Trying to resend keys.");
			if (!waitForElement(driver, 10, locator))
				throw new ScriptExecuteException("Current locator specifies non-interactive element. Input couldn't be resend");
			input.clear();
			sendText(input, text, driver, locator, retries + 1, true, needClick);
		}
	}
	
	protected String checkHoldKeys(String text)
	{
		if (StringUtils.isEmpty(text))
			return text;
		
		Matcher holdMatcher = HOLD_KEYS_PATTERN.matcher(text);
		while (holdMatcher.find())
		{
			boolean replace = false;
			String match = holdMatcher.group();
			String holdKey = holdMatcher.group(1);
			switch (holdKey)
			{
				case SendKeysHandler.SHIFT:
					if (!holdShift)
					{
						logInfo("Shift key will be holded during keys sending");
						holdShift = replace = true;
					}
					break;
				case SendKeysHandler.CTRL:
					if (!holdCtrl)
					{
						logInfo("Ctrl key will be holded during keys sending");
						holdCtrl = replace = true;
					}
					break;
				case SendKeysHandler.ALT:
					if (!holdAlt)
					{
						logInfo("Alt key will be holded during keys sending");
						holdAlt = replace = true;
					}
					break;
			}
			if (replace)
				text = text.replace(match, "");
		}
		return text;
	}

	protected void doClick(Actions a, WebElement element)
	{
		a.moveToElement(element);
		a.click();
		a.build().perform();
	}
	
	protected void doHoldKeys(WebDriver driver)
	{
		Actions a = new Actions(driver);
		if (holdShift)
			a.keyDown(Keys.SHIFT).perform();
		if (holdCtrl)
			a.keyDown(Keys.CONTROL).perform();
		if (holdAlt)
			a.keyDown(Keys.ALT).perform();
	}

	protected void doReleaseKeys(WebDriver driver)
	{
		Actions a = new Actions(driver);
		if (holdShift)
			a.keyUp(Keys.SHIFT).perform();
		if (holdCtrl)
			a.keyUp(Keys.CONTROL).perform();
		if (holdAlt)
			a.keyUp(Keys.ALT).perform();
	}
	
	protected boolean needEnable(WebElement element, Map<String, String> params)
	{
		if (element.isEnabled())
			return false;
		return RhUtils.getBooleanOrDefault(params, CAN_BE_DISABLED, false);
	}

	protected boolean needRun(WebDriver webDriver, Map<String, String> params) throws ScriptExecuteException
	{
		if (StringUtils.isEmpty(params.get(PARAM_WAIT2)))
			return false;

		int wait2 = getIntegerParam(params, PARAM_WAIT2);
		String locator2Name = params.get(PARAM_LOCATOR2), matcher2 = params.get(PARAM_MATCHER2);
		if (StringUtils.isEmpty(locator2Name) || StringUtils.isEmpty(matcher2))
		{
			Wait.webWait(webDriver, wait2);
		}
		else
		{
			try
			{
				By locator2 = WebLocatorsMapping.getByName(locator2Name).getWebLocator(webDriver, matcher2);
				if (!waitForElement(webDriver, wait2, locator2))
					return false;
			}
			catch (ScriptCompileException e)
			{
				throw new ScriptExecuteException("Error while resolving locator2", e);
			}
		}
		return true;
	}
	
	protected void enable(WebDriver driver, WebElement input)
	{
		logInfo("Trying to enable element");
		((JavascriptExecutor)driver).executeScript("arguments[0].removeAttribute('disabled')", input);
		logInfo("Element is " + (input.isEnabled() ? "enabled" : "still disabled"));
	}
	
	protected void disable(WebDriver driver, WebElement input)
	{
		logInfo("Try to disable element");
		((JavascriptExecutor)driver).executeScript("arguments[0].setAttribute('disabled', '')", input);
		logInfo("Now element is " + (input.isEnabled() ? "still enabled" : "disabled"));
	}

	protected static String replaceConversions(String src)
	{
		if (StringUtils.isEmpty(src))
			return "";
		return src.replace("(","#openbracket#")
				.replace("$rhGenerated", Configuration.getInstance().getFileStorage().getAbsolutePath());
	}
}
