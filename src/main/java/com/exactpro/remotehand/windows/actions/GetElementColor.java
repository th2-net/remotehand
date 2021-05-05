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

package com.exactpro.remotehand.windows.actions;

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.windows.ElementSearcher;
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Map;

public class GetElementColor extends WindowsAction {
	protected static final Logger loggerInstance = LoggerFactory.getLogger(GetElementColor.class);

	public static final String X_OFFSET = "xoffset", Y_OFFSET = "yoffset",
			RESPONSE_FORMAT = "format", DEFAULT_RESPONSE_FORMAT = "%s;%s;%s";


	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params,
	                  WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException {
		Point displacedPoint = getDisplacedPoint(params);
		WindowsDriver<?> driver = driverWrapper.getDriver();
		ElementSearcher es = new ElementSearcher(params, driver, cachedElements);
		WebElement element = es.searchElement();
		if (element == null)
			throw new ScriptExecuteException("Error while extracting color of element. Element not found");
		Color elementColor = screenWriter.getElementColor(driver, element, displacedPoint);

		return getFormattedResult(elementColor);
	}


	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}


	private Point getDisplacedPoint(Map<String, String> params) throws ScriptExecuteException {
		String xOffsetString = params.get(X_OFFSET);
		String yOffsetString = params.get(Y_OFFSET);

		if (!StringUtils.isEmpty(xOffsetString) && !StringUtils.isEmpty(yOffsetString)) {
			int xOffset = Integer.parseInt(xOffsetString);
			int yOffset = Integer.parseInt(yOffsetString);

			if (xOffset < 0 || yOffset < 0)
				throw new ScriptExecuteException("Co-ordinates cannot be negative");

			return new Point(xOffset, yOffset);
		}

		return null;
	}

	private String getFormattedResult(Color elementColor) {
		String format = getParams().getOrDefault(RESPONSE_FORMAT, DEFAULT_RESPONSE_FORMAT);
		return String.format(format, elementColor.getRed(), elementColor.getGreen(), elementColor.getBlue());
	}
}
