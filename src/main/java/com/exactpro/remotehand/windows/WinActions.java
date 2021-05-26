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

public class WinActions extends Actions {
	
	private WebDriver driver;
	
	public WinActions(WebDriver driver) {
		super(driver);
		this.driver = driver;
	}
	
	public static WinActions createAndCheck(WebDriver driver, WebElement... elements) throws ScriptExecuteException {
		WinActions actions = new WinActions(driver);
		actions.checkElements(elements);
		return actions;
	}
	
	public void checkElements(WebElement... elements) throws ScriptExecuteException {
		if (elements == null)
			return;

		for (WebElement element : elements) {
			if (element instanceof RemoteWebElement) {
				RemoteWebElement rwe = (RemoteWebElement) element;
				if (rwe.getWrappedDriver() != this.driver) {
					throw new ScriptExecuteException("Element was found by different driver than attached to actions");
				}
			}
			 
		}
	}
}
