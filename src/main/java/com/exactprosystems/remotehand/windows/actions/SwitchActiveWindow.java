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
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.NoSuchWindowException;
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
			MAX_TIMEOUT_PARAM = "maxtimeout";
	private static final String HANDLE_ATTRIBUTE = "NativeWindowHandle";

	private static final Logger loggerInstance = LoggerFactory.getLogger(SwitchActiveWindow.class);

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {

		// Action is useful for login windows, where login window belongs to the same process as
		// main application. Action is able to catch required window even if there are multiple windows opened
		// that satisfy chosen criteria.
		// --------------------------------------------------------------------------------------
		// If login window is closed, driver can throw NoSuchWindowException (Currently selected window has been closed)
		// so we catch it and continue searching
		
		
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
		boolean firstIt = true;
		try {
			
			do {
				if (!firstIt) {
					Thread.sleep(1000);
				}

				String currentHandle;
				try {
					if (isCurrentWindowExpected(driver, targetWindowMatcher, byName)) {
						this.logger.debug("Current window has the same title as expected one");
						return null;
					}

					currentHandle = driver.getWindowHandle();
				} catch (NoSuchWindowException e) {
					logger.debug("Current window was closed");
					currentHandle = null;
				}
				
				Set<String> handles = new LinkedHashSet<>(driver.getWindowHandles());

				this.logger.debug("Current handle: {}", currentHandle);
				this.logger.debug("Handles: {}", handles);

				if (currentHandle != null)
					handles.remove(currentHandle);

				for (String handle : handles) {
					String title = null;
					try {
						driver.switchTo().window(handle);
						title = driver.getTitle();
						this.logger.debug("Window {} title {}", handle, title);
						if (isCurrentWindowExpected(driver, targetWindowMatcher, byName)) {
							this.logger.debug("Window found");
							return null;
						}
					} catch (NoSuchWindowException e) {
						logger.debug("Window handle: {} title: {} was closed", handle, title);
					}
				}

				firstIt = false;
			} while (maxTimeout >= 0 && (startTime + maxTimeout) >= System.currentTimeMillis());
			
		} catch (InterruptedException e) {
			logger.warn("Interrupted", e);
		}

		throw new ScriptExecuteException("Cannot switch to specified window by " + 
				(byName ? "name" : "accessibilityId") + " '" + targetWindowMatcher + "'");
	}
	
	private boolean isCurrentWindowExpected(WindowsDriver<?> root, String matcher, boolean byName) {
		if (byName) {
			return matcher.equals(root.getTitle());
		} else {
			List<?> elementsByAccessibilityId = root.findElementsByAccessibilityId(matcher);
			return elementsByAccessibilityId != null && !elementsByAccessibilityId.isEmpty();
		}
	}

	@Override
	public Logger getLoggerInstance() {
		return loggerInstance;
	}

}
