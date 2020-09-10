/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.windows.ElementSearcher;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Click extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(Click.class);

	private static final String LEFT = "left", RIGHT = "right", MIDDLE = "middle", DOUBLE="double", BUTTON = "button",
			X_OFFSET = "xoffset", Y_OFFSET = "yoffset", MODIFIERS = "modifiers";

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {
		
		ElementSearcher es = new ElementSearcher();
		WebElement element = es.searchElement(params, driverWrapper.getDriver(), cachedWebElements);

		String button = params.get(BUTTON);
		if (button == null)
			button = LEFT;

		String xOffsetStr, yOffsetStr;
		int xOffset = 0, yOffset = 0;
		xOffsetStr = params.get(X_OFFSET);
		yOffsetStr = params.get(Y_OFFSET);

		Actions actions = new Actions(driverWrapper.getDriver());

		if ((xOffsetStr != null && !xOffsetStr.isEmpty()) && (yOffsetStr != null && !yOffsetStr.isEmpty()))
		{
			try
			{
				xOffset = Integer.parseInt(xOffsetStr);
				yOffset = Integer.parseInt(yOffsetStr);
			}
			catch (Exception e)
			{
				this.logger.error("xoffset or yoffset is not integer value");
			}
			actions = actions.moveToElement(element, xOffset, yOffset);
		}
		else
			actions = actions.moveToElement(element);

		if (button.equals(LEFT))
			actions.click();
		else if (button.equals(RIGHT))
			actions.contextClick();
		else if (button.equals(MIDDLE))
		{
			this.logger.error("Middle click is not implemented.");
			return null;
		}
		else if (button.equals(DOUBLE))
			actions.doubleClick();
		else
		{
			this.logger.error("Button may be only left, right, middle or double (for double click with left button).");
			return null;
		}

		actions.perform();
		
		return null;
	}

	@Override
	public Logger getLoggerInstance() {
		return loggerInstance;
	}
}
