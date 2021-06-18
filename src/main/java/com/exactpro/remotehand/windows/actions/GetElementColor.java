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
import com.exactpro.remotehand.windows.*;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Map;

import static com.exactpro.remotehand.screenwriter.ScreenWriter.convertToHex;

public class GetElementColor extends WindowsAction {
	private static final Logger loggerInstance = LoggerFactory.getLogger(GetElementColor.class);

	public static final String X_OFFSET = "xoffset", Y_OFFSET = "yoffset";


	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params,
	                  WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException {
		WindowsDriver<?> driver = this.getDriver(driverWrapper);
		ElementSearcher es = new ElementSearcher(params, driver, cachedElements);
		WebElement element = es.searchElement();
		if (element == null)
			throw new ScriptExecuteException("Error while extracting color of element. Element not found");
		ElementOffsetUtils.ElementOffsetParams elementOffsetParams
				= new ElementOffsetUtils.ElementOffsetParams(element, params.get(X_OFFSET), params.get(Y_OFFSET));
		Color elementColor = screenWriter.getElementColor(driver, ElementOffsetUtils.calculateOffset(elementOffsetParams));

		return convertToHex(elementColor);
	}


	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
