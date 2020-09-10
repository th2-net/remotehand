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

import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Open extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(Open.class);
	
	@Override
	public Logger getLoggerInstance() {
		return loggerInstance;
	}

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) {
		
		// closing previous drivers
		if (driverWrapper.getDriverNullable() != null) {
			this.logger.debug("Disposing previously created drivers");
			try {
				driverWrapper.getDriverNullable().close();
			} catch (Exception e) {
				logger.warn("Error while disposing driver", e);
			}
			driverWrapper.setDriver(null);
		}
		this.logger.debug("Creating new driver");
		
		String workDir = params.get("workdir");
		String execFile = params.get("execfile");

		Path workDirPath = Paths.get(workDir).normalize();
		Path exeFilePath = workDirPath.resolve(execFile).normalize();

		DesiredCapabilities capabilities = driverWrapper.createCommonCapabilities();
		capabilities.setCapability("app", exeFilePath.toString().replace('/', '\\'));
		capabilities.setCapability("appWorkingDir", workDirPath.toString().replace('/', '\\'));

		capabilities.setCapability("ms:experimental-webdriver", driverWrapper.isExperimentalDriver());
		if (driverWrapper.getWaitForApp() != null) {
			capabilities.setCapability("ms:waitForAppLaunch", driverWrapper.getWaitForApp());
		}
		if (driverWrapper.getCreateSessionTimeout() != null) {
			capabilities.setCapability("createSessionTimeout", driverWrapper.getCreateSessionTimeout());
		}
		if (driverWrapper.getNewCommandTimeout() != null) {
			capabilities.setCapability("newCommandTimeout", driverWrapper.getNewCommandTimeout());
		}
		
		driverWrapper.setDriver(driverWrapper.newDriver(capabilities));
		this.logger.debug("New driver created");
		return null;
	}

}
