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

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsManager;
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public class SwitchActiveWindow extends WindowsAction {

	private static final String WINDOW_NAME_PARAM = "windowname",
			ACCESSIBILITY_ID_PARAM = "accessibilityid",
			MAX_TIMEOUT_PARAM = "maxTimeout";
	private static final String HANDLE_ATTRIBUTE = "NativeWindowHandle";

	private static final Logger loggerInstance = LoggerFactory.getLogger(SwitchActiveWindow.class);

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {

		String maxTimeoutStr = params.get(MAX_TIMEOUT_PARAM);
		int maxTimeout = (StringUtils.isNotEmpty(maxTimeoutStr)) ? Integer.parseInt(maxTimeoutStr) : 0;
		
		String targetWindowMatcher = params.get(WINDOW_NAME_PARAM);
		boolean byName;
		if (targetWindowMatcher == null)
		{
			targetWindowMatcher = params.get(ACCESSIBILITY_ID_PARAM);
			byName = false;
		}
		else
			byName = true;
		
		WindowsDriver<?> driver = driverWrapper.getDriver();
		
		long startTime = System.currentTimeMillis();
		do {
			if (isCurrentWindowExpected(driver, targetWindowMatcher, byName)) {
				this.logger.debug("Current window has same title that expected");
				return null;
			}

			String currentHandle = driver.getWindowHandle();
			Set<String> handles = new LinkedHashSet<>(driver.getWindowHandles());

			this.logger.debug("Current handle: {}", currentHandle);
			this.logger.debug("Handles: {}", handles);

			handles.remove(currentHandle);

			for (String handle : handles) {
				driver.switchTo().window(handle);
				String title = driver.getTitle();
				this.logger.debug("Window {} title {}", handle, title);
				if (isCurrentWindowExpected(driver, targetWindowMatcher, byName)) {
					this.logger.debug("Window found");
					return null;
				}
			}
			
		} while (maxTimeout >= 0 && (startTime + maxTimeout) >= System.currentTimeMillis());

		throw new ScriptExecuteException("Cannot switch to specified window by " + 
				(byName ? "name" : "accessibilityId") + " '" + targetWindowMatcher + "'");
	}
	
	private boolean isCurrentWindowExpected(WindowsDriver<?> root, String matcher, boolean byName) {
		if (byName) {
			return matcher.equals(root.getTitle());
		} else {
			List<?> elementsByAccessibilityId = root.findElementsByAccessibilityId(matcher);
			return !matcher.isEmpty();
		}
	}
	
	private Set<String> findWindows(WindowsDriver<?> root, String matcher, boolean byName) throws ScriptExecuteException
	{
		List<?> windows = byName ? root.findElementsByName(matcher) : root.findElementsByAccessibilityId(matcher);
		if ((windows == null) || windows.isEmpty())
			throw new ScriptExecuteException(format("There are no windows '%s' visible from Root session.", matcher));
		
		logger.debug("Listing windows '{}':", matcher);
		Set<String> handles = new HashSet<>();
		for (Object w : windows)
		{
			WebElement window = (WebElement) w;
			String handle = window.getAttribute(HANDLE_ATTRIBUTE);
			String handleHex = nativeWindowHandleToHex(handle);
			handles.add(handleHex);
			logger.debug("{}={}, hex={}", HANDLE_ATTRIBUTE, handle, handleHex);
		}
		return handles;
	}
	
	private String nativeWindowHandleToHex(String handle)
	{
		return "0x00" + Integer.toHexString(Integer.parseInt(handle)).toUpperCase();
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
