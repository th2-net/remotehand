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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Click extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(Click.class);
	
	private static final String LEFT = "left", RIGHT = "right", MIDDLE = "middle", BUTTON = "button",
			X_OFFSET = "xoffset", Y_OFFSET = "yoffset", MODIFIERS = "modifiers";
	
	private static final Map<String, CharSequence> MODIFIER_KEYS = new HashMap<String, CharSequence>() {{
		put(SendKeys.SHIFT, Keys.SHIFT);
		put(SendKeys.CTRL, Keys.CONTROL);
		put(SendKeys.ALT, Keys.ALT);
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
		
		String xOffsetStr, yOffsetStr;
		int xOffset = 0, yOffset = 0;
		xOffsetStr = params.get(X_OFFSET);
		yOffsetStr = params.get(Y_OFFSET);
		
		Actions actions = new Actions(webDriver);
		
		if ((xOffsetStr != null && !xOffsetStr.isEmpty()) && (yOffsetStr != null && !yOffsetStr.isEmpty()))
		{
			try
			{
				xOffset = Integer.valueOf(xOffsetStr);
				yOffset = Integer.valueOf(yOffsetStr);
			}
			catch (Exception e)
			{
				logError("xoffset or yoffset is not integer value");
			}
			actions = actions.moveToElement(element, xOffset, yOffset);
		}
		else
			actions = actions.moveToElement(element);
		logInfo("Moved to element: %s", webLocator);
		
		Set<CharSequence> mods = applyModifiers(actions, params.get(MODIFIERS));
		try
		{
			if (button.equals(LEFT))
				actions.click().perform();
			else if (button.equals(RIGHT))
				actions.contextClick().perform();
			else if (button.equals(MIDDLE))
			{
				logError("Middle click is not implemented.");
				return null;
			}
			else
			{
				logError("Button may be only left, right or middle.");
				return null;
			}
			
			logInfo("Clicked %s button on: '%s'.", button, webLocator);
		}
		catch (ElementNotVisibleException e)
		{
			logError("Element is not visible. Executing click by JavaScript command" ,e);
			JavascriptExecutor js = (JavascriptExecutor) webDriver;
			js.executeScript("arguments[0].click();", element);
		}
		finally
		{
			resetModifiers(actions, mods);
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