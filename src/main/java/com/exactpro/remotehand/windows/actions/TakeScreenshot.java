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

import com.exactpro.remotehand.ActionOutputType;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.windows.ElementSearcher;
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TakeScreenshot extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(TakeScreenshot.class);
	
	public static final String NAME_PARAM = "name";
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException {
		String screenshotName = params.get(NAME_PARAM);
		WindowsDriver<?> driver = this.getDriver(driverWrapper);
		ElementSearcher es = new ElementSearcher(params, driver, cachedElements);
		WebElement element = es.searchElement();
		return element == null
				? this.takeScreenshot(screenshotName)
				: screenWriter.takeAndSaveElementScreenshot(screenshotName, driver, element);
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}

	@Override
	public ActionOutputType getOutputType() {
		return ActionOutputType.SCREENSHOT;
	}
}
