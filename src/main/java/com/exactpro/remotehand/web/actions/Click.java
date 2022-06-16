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

package com.exactpro.remotehand.web.actions;

import com.exactpro.remotehand.ExtendedActions;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebAction;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Click extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(Click.class);
	
	private static final String LEFT = "left", RIGHT = "right", MIDDLE = "middle", DOUBLE="double", BUTTON = "button",
			X_OFFSET = "xoffset", Y_OFFSET = "yoffset", MODIFIERS = "modifiers";

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
		WebElement element = findElement(webDriver, webLocator);
		
		String button = params.get(BUTTON);
		if (button == null)
			button = LEFT;
		
		ExtendedActions actions = new ExtendedActions(webDriver);
		moveToElement(actions, element, params.get(X_OFFSET), params.get(Y_OFFSET));
		logInfo("Moved to element: %s", webLocator);
		
		try
		{
			//Building sequence of actions to perform
			Set<CharSequence> mods = actions.applyClickModifiers(params.get(MODIFIERS));
			if (button.equals(LEFT))
				actions.click();
			else if (button.equals(RIGHT))
				actions.contextClick();
			else if (button.equals(MIDDLE))
			{
				logError("Middle click is not implemented.");
				return null;
			}
			else if (button.equals(DOUBLE))
				actions.doubleClick();
			else
			{
				logError("Button may be only left, right, middle or double (for double click with left button).");
				return null;
			}
			actions.resetClickModifiers(mods);

			//Performing built sequence of actions
			actions.perform();
			
			logInfo("Clicked %s button on: '%s'.", button, webLocator);
		}
		catch (ElementNotVisibleException e)
		{
			logError("Element is not visible. Executing click by JavaScript command" ,e);
			JavascriptExecutor js = (JavascriptExecutor) webDriver;
			js.executeScript("arguments[0].click();", element);
		}
		return null;
	}
	
	@Override
	protected boolean waitForElement(WebDriver driver, int waitDuration, By locator) throws ScriptExecuteException
	{
		try
		{
			new WebDriverWait(driver, waitDuration).until(ExpectedConditions.elementToBeClickable(locator));
			logInfo("Appeared locator: '%s'.", locator);
		}
		catch (TimeoutException ex)
		{
			List<WebElement> elements = driver.findElements(locator);
			if (elements.size() > 0)
				logWarn("Element is not clickable, but will try to click on it anyway");
			else if (isElementMandatory())
				throw new ScriptExecuteException("Timed out after " + waitDuration + " seconds waiting for '" + locator.toString() + "'");
			else
				return false;
		}
		return true;
	}

	private void moveToElement(ExtendedActions actions, WebElement element, String xOffsetStr, String yOffsetStr) {
		int xOffset = 0, yOffset = 0;
		try {
			xOffset = Integer.parseInt(xOffsetStr);
			yOffset = Integer.parseInt(yOffsetStr);
		} catch (NumberFormatException e) {
			logError("xoffset or yoffset is not integer value");
		}

		if (xOffset == 0 && yOffset == 0) {
			actions.moveToElement(element);
		} else {
			WebDriver driver = actions.getAttachedDriver();
			if (driver instanceof ChromeDriver && getChromeDriverVersion((ChromeDriver) driver) > 74) {
				xOffset -= element.getSize().getWidth() / 2;
				yOffset -= element.getSize().getHeight() / 2;
			}

			actions.moveToElement(element, xOffset, yOffset);
		}
	}
}