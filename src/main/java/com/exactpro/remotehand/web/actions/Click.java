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

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebAction;
import com.exactpro.remotehand.web.utils.SendKeysHandler;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Click extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(Click.class);
	
	private static final String LEFT = "left", RIGHT = "right", MIDDLE = "middle", DOUBLE="double", BUTTON = "button",
			X_OFFSET = "xoffset", Y_OFFSET = "yoffset", MODIFIERS = "modifiers";
	
	private static final Map<String, CharSequence> MODIFIER_KEYS = new HashMap<String, CharSequence>() {{
		put(SendKeysHandler.SHIFT, Keys.SHIFT);
		put(SendKeysHandler.CTRL, Keys.CONTROL);
		put(SendKeysHandler.ALT, Keys.ALT);
	}};
	
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
		
		Actions actions = moveToElement(webDriver, element, params.get(X_OFFSET), params.get(Y_OFFSET));
		logInfo("Moved to element: %s", webLocator);
		
		try
		{
			//Building sequence of actions to perform
			Set<CharSequence> mods = applyModifiers(actions, params.get(MODIFIERS));
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
			resetModifiers(actions, mods);
			
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
	
	private Actions moveToElement(WebDriver webDriver, WebElement element, String xOffsetStr, String yOffsetStr)
	{
		Actions actions = new Actions(webDriver);
		
		if (StringUtils.isNotBlank(xOffsetStr) && StringUtils.isNotBlank(yOffsetStr))
		{
			int xOffset = 0, yOffset = 0;
			try
			{
				xOffset = Integer.parseInt(xOffsetStr);
				yOffset = Integer.parseInt(yOffsetStr);
			}
			catch (NumberFormatException e)
			{
				logError("xoffset or yoffset is not integer value");
			}
			
			if (webDriver instanceof ChromeDriver && getChromeDriverVersion((ChromeDriver) webDriver) > 74)
			{
				xOffset -= element.getSize().getWidth() / 2;
				yOffset -= element.getSize().getHeight() / 2;
			}
			return actions.moveToElement(element, xOffset, yOffset);
		}
		return actions.moveToElement(element);
	}
	
	private Set<CharSequence> applyModifiers(Actions actions, String modifiers)
	{
		if (StringUtils.isEmpty(modifiers))
			return null;
		
		Set<CharSequence> result = new LinkedHashSet<>();
		String[] keys = modifiers.split(",");
		for (String k : keys)
		{
			CharSequence c = MODIFIER_KEYS.get(k.trim());
			if (c == null || result.contains(c))
				continue;
			actions.keyDown(c);
			result.add(c);
		}
		return result;
	}
	
	private void resetModifiers(Actions actions, Set<CharSequence> modifiers)
	{
		if (modifiers == null || modifiers.isEmpty())
			return;
		
		for (CharSequence c : modifiers)
			actions.keyUp(c);
	}
}
