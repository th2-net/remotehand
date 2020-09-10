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
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class GetWindow extends WindowsAction {

	private static final String WINDOW_NAME_PARAM = "windowname";

	private static final Logger loggerInstance = LoggerFactory.getLogger(SwitchActiveWindow.class);

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {

		String targetWindowName = params.get(WINDOW_NAME_PARAM);

		DesiredCapabilities commonCapabilities = driverWrapper.createCommonCapabilities();
		commonCapabilities.setCapability("app", "Root");
		WindowsDriver<?> driver1 = null;
		try {
			driver1 = driverWrapper.newDriver(commonCapabilities);
			List<? extends WebElement> elements = driver1.findElementsByName(targetWindowName);
			if (elements.size() == 1) {

				String handleString = elements.iterator().next().getAttribute("NativeWindowHandle");
				logger.debug("Handle str for window : {} {}", targetWindowName, handleString);
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
				String errorText = String.format("Found %s windows with name %s", elements.size(), targetWindowName);
				logger.error(errorText);
				throw new ScriptExecuteException(errorText);
			}

		} finally {
			if (driver1 != null) {
				try {
					driver1.close();
				} catch (Exception e1) {
					logger.error("Error while disposing driver", e1);
				}
			}
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
