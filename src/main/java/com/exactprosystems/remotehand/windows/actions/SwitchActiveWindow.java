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
import com.exactprosystems.remotehand.windows.WindowsManager;
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public class SwitchActiveWindow extends WindowsAction {

	private static final String WINDOW_NAME_PARAM = "windowname";
	private static final String HANDLE_ATTRIBUTE = "NativeWindowHandle";

	private static final Logger loggerInstance = LoggerFactory.getLogger(SwitchActiveWindow.class);

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {

		String targetWindowName = params.get(WINDOW_NAME_PARAM);
		WindowsDriver<?> driver = driverWrapper.getDriver();
		if (targetWindowName.equals(driver.getTitle())) {
			this.logger.debug("Current window has same title that expected");
			return null;
		}

		WindowsDriver<?> root = driverWrapper.getOrCreateRootDriver();
		Set<String> allWindowHandles = findWindowsByName(root, targetWindowName);

		WindowsManager windowsManager = windowsSessionContext.getWindowsManager();
		String windowHandle = windowsManager.findWindowForSession(targetWindowName, 
				windowsSessionContext.getSessionId(), allWindowHandles);
		driverWrapper.changeDriverForNewMainWindow(windowHandle);
		
		return null;
	}
	
	private Set<String> findWindowsByName(WindowsDriver<?> root, String name) throws ScriptExecuteException
	{
		List<?> windows = root.findElementsByName(name);
		if ((windows == null) || windows.isEmpty())
			throw new ScriptExecuteException(format("There are no windows '%s' visible from Root session.", name));
		
		logger.debug("Listing windows '{}':", name);
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
