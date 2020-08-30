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
import com.exactprosystems.remotehand.windows.ElementSearcher;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ClickContextMenu extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(ClickContextMenu.class);
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params) throws ScriptExecuteException {

		DesiredCapabilities commonCapabilities = driverWrapper.createCommonCapabilities();
		commonCapabilities.setCapability("app", "Root");
		WindowsDriver<?> driver = null;
		try {
			logger.debug("Creating new driver for clicking context menu");
			driver = driverWrapper.newDriver(commonCapabilities);
			
			ElementSearcher es = new ElementSearcher();
			WebElement element = es.searchElement(params, driver);
			
			element.click();
		} finally {
			if (driver != null)
				driver.close();
		}
		
		return null;
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
