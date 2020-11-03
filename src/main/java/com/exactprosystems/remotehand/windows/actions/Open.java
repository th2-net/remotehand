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

import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.remote.MobileCapabilityType;
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
		capabilities.setCapability(MobileCapabilityType.APP, exeFilePath.toString().replace('/', '\\'));
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
