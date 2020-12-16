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

package com.exactprosystems.remotehand.windows.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.windows.ElementSearcher;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Click extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(Click.class);

	private static final String LEFT = "left", RIGHT = "right", MIDDLE = "middle", DOUBLE="double", BUTTON = "button",
			X_OFFSET = "xoffset", Y_OFFSET = "yoffset", MODIFIERS = "modifiers", ATTACHED_BORDER = "attachedborder",
			LEFT_TOP = "left_top", RIGHT_TOP = "right_top", LEFT_BOTTOM = "left_bottom", RIGHT_BOTTOM = "right_bottom";

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {
		
		ElementSearcher es = new ElementSearcher(params, driverWrapper.getDriver(), cachedWebElements);
		WebElement element = es.searchElement();

		String button = params.get(BUTTON);
		if (button == null)
			button = LEFT;

		String xOffsetStr, yOffsetStr;
		int xOffset = 0, yOffset = 0;
		xOffsetStr = params.get(X_OFFSET);
		yOffsetStr = params.get(Y_OFFSET);

		Actions actions = new Actions(driverWrapper.getDriver());
		
		String fromBorder = params.get(ATTACHED_BORDER);
		if (fromBorder != null && !fromBorder.isEmpty()) {
			Dimension rect = element.getSize();
			switch (fromBorder) {
				case RIGHT_TOP:
					xOffset = rect.getWidth();
					yOffset = 0;
					break;
				case RIGHT_BOTTOM:
					xOffset = rect.getWidth();
					yOffset = rect.getHeight();
					break;
				case LEFT_BOTTOM:
					xOffset = 0;
					yOffset = rect.getHeight();
					break;
				case LEFT_TOP:
					xOffset = 0;
					yOffset = 0;
					break;
				default:
					throw new ScriptExecuteException("Unrecognized option: attachedBorder: " + fromBorder);
			}
		}

		if ((xOffsetStr != null && !xOffsetStr.isEmpty()) && (yOffsetStr != null && !yOffsetStr.isEmpty()))
		{
			try
			{
				xOffset += Integer.parseInt(xOffsetStr);
				yOffset += Integer.parseInt(yOffsetStr);
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
