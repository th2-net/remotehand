////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web.actions;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class Click extends WebAction
{
	private static final Logger logger = Logger.getLogger(Click.class);
	private static final String LEFT = "left", RIGHT = "right", MIDDLE = "middle", BUTTON = "button",
			X_OFFSET = "xoffset", Y_OFFSET = "yoffset";
	
	
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
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement element = findElement(webDriver, webLocator);
		
		String button = params.get(BUTTON);
		if (button == null)
			button = LEFT;
		
		try {
			if (button.equals(LEFT))
				element.click();
			else if (button.equals(RIGHT))
			{
				String xOffsetStr, yOffsetStr;
				int xOffset = 0, yOffset = 0;
				xOffsetStr = params.get(X_OFFSET);
				yOffsetStr = params.get(Y_OFFSET);
				
				Actions actions = new Actions(webDriver);
				
				if (xOffsetStr != null && !xOffsetStr.isEmpty() && Y_OFFSET != null && !yOffsetStr.isEmpty())
				{
					try
					{
						xOffset = Integer.valueOf(xOffsetStr);
						yOffset = Integer.valueOf(yOffsetStr);
					}
					catch (Exception e)
					{
						logger.error("xOffset or yOffset is not integer value");
					}
					actions.moveToElement(element,xOffset,yOffset).contextClick().perform();
				}
				else
				{
					actions.contextClick(element).perform();
				}

				//actions.moveToElement(element,xOffset,yOffset).contextClick().perform();;
				
				//actions.contextClick(element);
				//actions.perform();
			}
			else if (button.equals(MIDDLE))
			{
				
			}
			else
			{
				logger.error("Button may be only left, right or middle.");
				return null;
			}
			
			logger.info("Clicked " + button + " button " + "on: '" + element.toString() + "'");
				
		} catch (ElementNotVisibleException e) {
			logger.error("Element is not visible" ,e);

			JavascriptExecutor js = (JavascriptExecutor) webDriver;
			js.executeScript("arguments[0].click();", element);

		}

		

		return null;
	}
}
