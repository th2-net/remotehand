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
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class GetWindow extends WindowsAction {

	private static final String WINDOW_NAME_PARAM = "windowname",
			ACCESSIBILITY_ID_PARAM = "accessibilityid";

	private static final Logger loggerInstance = LoggerFactory.getLogger(SwitchActiveWindow.class);

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {

		String targetWindowMatcher = params.get(WINDOW_NAME_PARAM);
		boolean byName;
		if (targetWindowMatcher == null)
		{
			targetWindowMatcher = params.get(ACCESSIBILITY_ID_PARAM);
			byName = false;
		}
		else
			byName = true;

		WindowsDriver<?> driver1 = driverWrapper.getOrCreateRootDriver();
		List<? extends WebElement> elements = byName ? driver1.findElementsByName(targetWindowMatcher) : driver1.findElementsByAccessibilityId(targetWindowMatcher);
		if (elements.size() == 1) {
			String handleString = elements.iterator().next().getAttribute("NativeWindowHandle");
			logger.debug("Handle str for window : {} {}", targetWindowMatcher, handleString);
			int handleInt = Integer.parseInt(handleString);
			String handleHex = Integer.toHexString(handleInt);

			boolean newDriver = true;
			if (driverWrapper.getDriverNullable() != null) {
				try {
					driverWrapper.getDriver().switchTo().window(handleHex);
					newDriver = false;
				} catch (Exception e) {
					logger.error("Error while switching window", e);
					try {
						driverWrapper.getDriverNullable().close();
					} catch (Exception e1) {
						logger.error("Error while closing driver", e1);
					}
					newDriver = true;
				}
			}

			if (newDriver) {
				DesiredCapabilities capabilities = driverWrapper.createCommonCapabilities();
				capabilities.setCapability("appTopLevelWindow", handleHex);

				capabilities.setCapability("ms:experimental-webdriver", driverWrapper.isExperimentalDriver());
				if (driverWrapper.getCreateSessionTimeout() != null) {
					capabilities.setCapability("createSessionTimeout", driverWrapper.getCreateSessionTimeout());
				}
				if (driverWrapper.getNewCommandTimeout() != null) {
					capabilities.setCapability("newCommandTimeout", driverWrapper.getNewCommandTimeout());
				}
				driverWrapper.setDriver(driverWrapper.newDriver(capabilities));
			}
			return null;
		} else {
			String errorText = String.format("Found %s windows with name %s", elements.size(), targetWindowMatcher);
			logger.error(errorText);
			throw new ScriptExecuteException(errorText);
		}
	}

	@Override
	public Logger getLoggerInstance() {
		return loggerInstance;
	}

	@Override
	protected String[] mandatoryParams() {
		return new String[] { WINDOW_NAME_PARAM };
	}
}
