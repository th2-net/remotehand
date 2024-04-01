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

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by alexey.suknatov on 4/3/17.
 */
public class SwitchWindow extends WebAction {
	private final static String WINDOW = "window";

	public SwitchWindow() {
		super(false, true, WINDOW);
	}

	@Override
	protected boolean waitForElement(WebDriver webDriver, int seconds, By webLocator) throws ScriptExecuteException {
		final int expectedNumber = getIntegerParam(getParams(), WINDOW) + 1;
		Boolean findWindow;
		try {
			findWindow = (new WebDriverWait(webDriver, seconds)).until((ExpectedCondition<Boolean>) driver -> {
                try {
                    return driver.getWindowHandles().size() >= expectedNumber;
                } catch (WebDriverException e) {
                    logger.error("Error while waiting for window #"+expectedNumber+" to open", e);
                    return false;
                }
            });
		} catch (TimeoutException ex) {
			throw new ScriptExecuteException("Timed out after " + seconds +
					". Actual number of open windows is: " + webDriver.getWindowHandles().size() +
					". Expected: " + expectedNumber);
		}

		if (!findWindow)
			throw new ScriptExecuteException("Actual number of open windows is: " + webDriver.getWindowHandles().size() + ". Expected: " + expectedNumber);

		logger.info("Number of open windows: '{}'", webDriver.getWindowHandles().size());
		return true;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		Set<String> windowHandles = webDriver.getWindowHandles();
		Iterator<String> iterator = windowHandles.iterator();
		if (iterator.hasNext()) {
			int windowNumber = getIntegerParam(params, WINDOW);
			if (windowNumber < 0 || windowNumber > windowHandles.size() - 1) {
				String errorMessage = "There is no such window: " + windowNumber;
				logger.error(errorMessage);
				throw new ScriptExecuteException(errorMessage);
			} else {
				String windowHandle = iterator.next();
				for (int i = 0; i < windowNumber; i++) {
					windowHandle = iterator.next();
				}
				webDriver.switchTo().window(windowHandle);
				logger.debug("Child number is: " + windowNumber);
				webDriver.switchTo().window(windowHandle);
			}
		}
		return null;
	}
}