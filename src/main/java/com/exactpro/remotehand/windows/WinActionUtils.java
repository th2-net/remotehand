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

package com.exactpro.remotehand.windows;

import com.exactpro.remotehand.ScriptExecuteException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WinActionUtils {

	private static final Logger logger = LoggerFactory.getLogger(WinActionUtils.class);

	public static WinActions createAndCheck(WebDriver driver, WebElement... elements) throws ScriptExecuteException {
		WinActions actions = new WinActions(driver);
		checkElements(actions, elements);
		return actions;
	}

	public static void checkElements(Actions actions, WebElement... elements) throws ScriptExecuteException {
		if (elements == null)
			return;
		
		if (!(actions instanceof WinActions)) {
			logger.error("Incorrect instance of actions. Expected {} Actual {}", WinActions.class.getName(), 
					actions.getClass().getName());
			throw new ScriptExecuteException("Incorrect instance of actions");
		}

		WinActions winActions = (WinActions) actions;
		for (WebElement element : elements) {
			if (element instanceof RemoteWebElement) {
				RemoteWebElement rwe = (RemoteWebElement) element;
				if (rwe.getWrappedDriver() != winActions.getAttachedDriver()) {
					throw new ScriptExecuteException("Element was found by different driver than attached to actions");
				}
			}

		}
	}
	
}
