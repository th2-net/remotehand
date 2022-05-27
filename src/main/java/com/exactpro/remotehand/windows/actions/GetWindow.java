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

import com.exactpro.remotehand.utils.RhUtils;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.windows.WADCapabilityType;
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class GetWindow extends WindowsAction {

	private static final String WINDOW_NAME_PARAM = "windowname",
			ACCESSIBILITY_ID_PARAM = "accessibilityid";
	private static final String HANDLE_ATTRIBUTE = "NativeWindowHandle";

	private static final Logger loggerInstance = LoggerFactory.getLogger(GetWindow.class);

	private String getHandleString(final WebElement window) {
		final String handleString = window.getAttribute(HANDLE_ATTRIBUTE);
		if (handleString != null) return handleString;

		// trying to workaround problem with getting window handle string
		if (!(window instanceof RemoteWebElement)) return null;

		final String id = ((RemoteWebElement) window).getId();
		if (id == null) return null;

		final int handleIdx = id.indexOf('.') + 1;
		if (handleIdx == 0 || handleIdx == id.length()) return null;

		return id.substring(handleIdx);
	}

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

		boolean experimental = RhUtils.getBooleanOrDefault(params, EXPERIMENTAL_PARAM, true);
		WindowsDriver<?> driver1 = driverWrapper.getDriver(true, experimental);
		List<? extends WebElement> elements = byName ? driver1.findElementsByName(targetWindowMatcher) : driver1.findElementsByAccessibilityId(targetWindowMatcher);

		if (elements.size() == 1) {
			final String handleString = getHandleString(elements.iterator().next());

			logger.debug("Handle str for window : {} {}", targetWindowMatcher, handleString);

			if (handleString == null) {
				throw new ScriptExecuteException("Failed to get windows handle.");
			}

			int handleInt = Integer.parseInt(handleString);
			String handleHex = Integer.toHexString(handleInt);
			
			driverWrapper.resetWindowDrivers();
			
			DesiredCapabilities capabilities = driverWrapper.createCommonCapabilities();
			capabilities.setCapability(WADCapabilityType.APP_TOP_LEVEL, handleHex);
			
			if (driverWrapper.getCreateSessionTimeout() != null) {
				capabilities.setCapability(WADCapabilityType.CREATE_SESSION_TIMEOUT, driverWrapper.getCreateSessionTimeout());
			}
			if (driverWrapper.getNewCommandTimeout() != null) {
				capabilities.setCapability(WADCapabilityType.NEW_COMMAND_TIMEOUT, driverWrapper.getNewCommandTimeout());
			}
			driverWrapper.createDriver(capabilities, true);
				
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
