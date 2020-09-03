/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import io.appium.java_client.windows.WindowsDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SwitchActiveWindow extends WindowsAction {

	private static final String WINDOW_NAME_PARAM = "windowname";

	private static final Logger loggerInstance = LoggerFactory.getLogger(SwitchActiveWindow.class);

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params) throws ScriptExecuteException {

		String targetWindowName = params.get(WINDOW_NAME_PARAM);
		WindowsDriver<?> driver = driverWrapper.getDriver();
		if (targetWindowName.equals(driver.getTitle())) {
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
			if (targetWindowName.equals(title)) {
				return null;
			}
		}

		throw new ScriptExecuteException("Cannot switch to specified window '" + targetWindowName + "'");
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
