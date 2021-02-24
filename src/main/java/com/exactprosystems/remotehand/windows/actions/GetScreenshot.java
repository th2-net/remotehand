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

package com.exactprosystems.remotehand.windows.actions;

import com.exactprosystems.remotehand.ActionOutputType;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.utils.ScreenshotUtils;
import com.exactprosystems.remotehand.windows.ElementSearcher;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GetScreenshot extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(GetScreenshot.class);
	
	public static final String NAME_PARAM = "name";
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException {
		String screenshotName = params.get(NAME_PARAM);
		ElementSearcher es = new ElementSearcher(params, driverWrapper.getDriver(), cachedElements);
		WebElement element = es.searchElement();
		String screenshotId;
		if (element == null) {
			screenshotId = this.takeScreenshot(screenshotName);
		} else {
			screenshotId = ScreenshotUtils.takeScreenshot(screenshotName, element);
		}
		return screenshotId;
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
