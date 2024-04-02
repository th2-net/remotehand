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

package com.exactpro.remotehand.web.actions;

import com.exactpro.remotehand.Configuration;
import com.exactpro.remotehand.utils.RhUtils;
import com.exactpro.remotehand.ScriptCompileException;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebAction;
import com.exactpro.remotehand.web.WebScriptCompiler;
import com.exactpro.remotehand.web.utils.SendKeysHandler;
import com.exactpro.remotehand.web.webelements.WebLocatorsMapping;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendKeys extends WebAction {
	private static final Pattern HOLD_KEYS_PATTERN = Pattern.compile("\\(#(shift|ctrl|alt)#\\)");

	private static final String PARAM_TEXT = "text";
	private static final String[] MANDATORY_PARAMS = { PARAM_TEXT };
	private static final String PARAM_TEXT2 = String.format("%s2", PARAM_TEXT);
	private static final String PARAM_WAIT2 = String.format("%s2", PARAM_WAIT);
	private static final String PARAM_LOCATOR2 = String.format("%s2", WebScriptCompiler.WEB_LOCATOR);
	private static final String PARAM_MATCHER2 = String.format("%s2", WebScriptCompiler.WEB_MATCHER);
	private static final String PARAM_CHECK_INPUT = "checkinput";
	private static final String PARAM_NEED_CLICK = "needclick";
	private static final String CLEAR_BEFORE = "clear";
	private static final String CAN_BE_DISABLED = "canbedisabled";

	private static final int MAX_RETRIES = Configuration.getInstance().getSendKeysMaxRetries();

	private boolean holdShift, holdCtrl, holdAlt;
	private final SendKeysHandler handler = new SendKeysHandler();

	public SendKeys() {
		super(true, true, MANDATORY_PARAMS);
	}

	@Override
	public boolean isCanSwitchPage() {
		return true;
	}

	public SendKeys(boolean locatorNeeded, boolean canWait, String... mandatoryParams) {
		super(locatorNeeded, canWait, mandatoryParams);
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		WebElement input = webLocator != null ? findElement(webDriver, webLocator) : webDriver.switchTo().activeElement();
		if (webLocator == null)
			logger.info("Active element: {}" , input != null ? input.getTagName() : "null");
		if (input == null)
			throw new ScriptExecuteException("Unable to send keys: input element is null");

		boolean shouldBeEnabled = needEnable(input, params);
		try {
			if (shouldBeEnabled)
				enable(webDriver, input);

			if (RhUtils.getBooleanOrDefault(params, CLEAR_BEFORE, false)) {
				input.clear();
				logger.info("Text field has been cleared.");
			}

			boolean checkInput = RhUtils.getBooleanOrDefault(params, PARAM_CHECK_INPUT, true);
			boolean needClick = RhUtils.getBooleanOrDefault(params, PARAM_NEED_CLICK, true);
			String text = replaceConversions(checkHoldKeys(params.get(PARAM_TEXT)));
			logger.info("Sending text1 ({}) to locator: {}", text, webLocator);
			sendText(input, text, webDriver, webLocator, 0, checkInput, needClick);
			logger.info("Text '{}' was sent to locator: {}.", text, webLocator);

			String text2 = replaceConversions(params.get(PARAM_TEXT2));
			if (StringUtils.isNotEmpty(text2) && needRun(webDriver, params)) {
				logger.info("Sending text2 to: {}", webLocator);
				sendText(input, text2, webDriver, webLocator, 0, checkInput, needClick);
				logger.info("Sent text2 to: {}", webLocator);
			}
		} finally {
			if (shouldBeEnabled)
				disable(webDriver, input);
		}

		return null;
	}

	private void sendText(
			WebElement input,
			String text,
			WebDriver driver,
			By locator,
			int retries,
			boolean checkInput,
			boolean needClick
	) throws ScriptExecuteException {
		List<String> strings = handler.processInputText(text);

		if (retries > 0) {
			logger.info("Trying to scroll input element into view...");
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);
		}

		Actions actions = new Actions(driver);

		if (needClick) {
			doClick(actions, input);
			// find element again to avoid 'cannot focus element' error
			input = locator != null ? findElement(driver, locator) : driver.switchTo().activeElement();
		}
		doHoldKeys(driver);

		for (String str : strings) {
			if (str.startsWith(SendKeysHandler.KEY_SIGN)) {
				handler.sendSpecialKey(actions, str, locator);
				continue;
			}

			String inputAtStart = input.getAttribute("value");
			handler.doSendKeys(actions, str);
			if (inputAtStart == null) {
				logger.warn("Input field does not contain value attribute. Sending text as is.");
				continue;
			}

			if (checkInput && driver instanceof ChromeDriver)
				checkInput(input, text, driver, locator, retries, needClick, str, inputAtStart);
		}
		doReleaseKeys(driver);
	}

	private void checkInput(WebElement input, String text, WebDriver driver, By locator,
							int retries, boolean needClick, String str, String inputAtStart
	) throws ScriptExecuteException {
		String result = input.getAttribute("value");
		boolean equals = result.equals(str);
		if (!equals && result.startsWith(inputAtStart))
			equals = result.replaceFirst(Pattern.quote(inputAtStart), "").equals(str);
		if (!equals) {
			if (retries >= MAX_RETRIES) {
				logger.warn("Missed input detected, but too many retries were already done.");
				logger.warn("Unable to send text '{}' to locator '{}'", text, locator);
				return;
			}

			// If field is not filled as expected for current moment, redo the whole operation
			logger.info("Missed input detected. Trying to resend keys.");
			if (!waitForElement(driver, 10, locator))
				throw new ScriptExecuteException("Current locator specifies non-interactive element. Input couldn't be resend");
			input.clear();
			sendText(input, text, driver, locator, retries + 1, true, needClick);
		}
	}

	private String checkHoldKeys(String text) {
		if (StringUtils.isEmpty(text))
			return text;

		Matcher holdMatcher = HOLD_KEYS_PATTERN.matcher(text);
		while (holdMatcher.find()) {
			boolean replace = false;
			String match = holdMatcher.group();
			String holdKey = holdMatcher.group(1);
			switch (holdKey) {
				case SendKeysHandler.SHIFT:
					if (!holdShift) {
						logger.info("Shift key will be held during keys sending");
						holdShift = replace = true;
					}
					break;
				case SendKeysHandler.CTRL:
					if (!holdCtrl) {
						logger.info("Ctrl key will be held during keys sending");
						holdCtrl = replace = true;
					}
					break;
				case SendKeysHandler.ALT:
					if (!holdAlt) {
						logger.info("Alt key will be held during keys sending");
						holdAlt = replace = true;
					}
					break;
			}
			if (replace)
				text = text.replace(match, "");
		}
		return text;
	}

	private void doClick(Actions a, WebElement element) {
		a.moveToElement(element);
		a.click();
		a.build().perform();
	}

	private void doHoldKeys(WebDriver driver) {
		Actions a = new Actions(driver);
		if (holdShift)
			a.keyDown(Keys.SHIFT).perform();
		if (holdCtrl)
			a.keyDown(Keys.CONTROL).perform();
		if (holdAlt)
			a.keyDown(Keys.ALT).perform();
	}

	private void doReleaseKeys(WebDriver driver) {
		Actions a = new Actions(driver);
		if (holdShift)
			a.keyUp(Keys.SHIFT).perform();
		if (holdCtrl)
			a.keyUp(Keys.CONTROL).perform();
		if (holdAlt)
			a.keyUp(Keys.ALT).perform();
	}

	private boolean needEnable(WebElement element, Map<String, String> params) throws ScriptExecuteException {
		if (element.isEnabled())
			return false;
		return RhUtils.getBooleanOrDefault(params, CAN_BE_DISABLED, false);
	}

	private boolean needRun(WebDriver webDriver, Map<String, String> params) throws ScriptExecuteException {
		if (StringUtils.isEmpty(params.get(PARAM_WAIT2)))
			return false;

		int wait2 = getIntegerParam(params, PARAM_WAIT2);
		String locator2Name = params.get(PARAM_LOCATOR2), matcher2 = params.get(PARAM_MATCHER2);
		if (StringUtils.isEmpty(locator2Name) || StringUtils.isEmpty(matcher2)) {
			Wait.webWait(webDriver, wait2);
		} else {
			try {
				By locator2 = WebLocatorsMapping.getByName(locator2Name).getWebLocator(webDriver, matcher2);
				if (!waitForElement(webDriver, wait2, locator2))
					return false;
			} catch (ScriptCompileException e) {
				throw new ScriptExecuteException("Error while resolving locator2", e);
			}
		}
		return true;
	}

	private void enable(WebDriver driver, WebElement input) {
		logger.info("Trying to enable element");
		((JavascriptExecutor)driver).executeScript("arguments[0].removeAttribute('disabled')", input);
		logger.info("Element is " + (input.isEnabled() ? "enabled" : "still disabled"));
	}

	private void disable(WebDriver driver, WebElement input) {
		logger.info("Try to disable element");
		((JavascriptExecutor)driver).executeScript("arguments[0].setAttribute('disabled', '')", input);
		logger.info("Now element is " + (input.isEnabled() ? "still enabled" : "disabled"));
	}

	private static String replaceConversions(String src) {
		if (StringUtils.isEmpty(src))
			return "";
		return src.replace("(","#openbracket#")
				.replace("$rhGenerated", Configuration.getInstance().getFileStorage().getAbsolutePath());
	}
}