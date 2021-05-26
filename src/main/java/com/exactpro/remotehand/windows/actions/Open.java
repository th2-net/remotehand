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

package com.exactpro.remotehand.windows.actions;

import com.exactpro.remotehand.windows.WADCapabilityType;
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext;
import org.apache.commons.lang3.StringUtils;
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
		driverWrapper.resetWindowDrivers();
		
		this.logger.debug("Creating new driver");
		
		String workDir = params.get("workdir");
		String execFile = params.get("execfile");
		String appArgs = params.get("appargs");

		Path workDirPath = Paths.get(workDir).normalize();
		Path exeFilePath = workDirPath.resolve(execFile).normalize();

		DesiredCapabilities capabilities = driverWrapper.createCommonCapabilities();
		capabilities.setCapability(WADCapabilityType.APP, exeFilePath.toString().replace('/', '\\'));
		capabilities.setCapability(WADCapabilityType.APP_WORKING_DIR, workDirPath.toString().replace('/', '\\'));
		if (StringUtils.isNotBlank(appArgs)) {
			capabilities.setCapability(WADCapabilityType.APP_ARGUMENTS, appArgs);
		}

		capabilities.setCapability(WADCapabilityType.EXPERIMENTAL_DRIVER, Boolean.TRUE.toString());
		if (driverWrapper.getWaitForApp() != null) {
			capabilities.setCapability(WADCapabilityType.WAIT_FOR_LAUNCH, driverWrapper.getWaitForApp());
		}
		if (driverWrapper.getCreateSessionTimeout() != null) {
			capabilities.setCapability(WADCapabilityType.CREATE_SESSION_TIMEOUT, driverWrapper.getCreateSessionTimeout());
		}
		if (driverWrapper.getNewCommandTimeout() != null) {
			capabilities.setCapability(WADCapabilityType.NEW_COMMAND_TIMEOUT, driverWrapper.getNewCommandTimeout());
		}
		
		driverWrapper.createDriver(capabilities, DEFAULT_EXPERIMENTAL);
		this.logger.debug("New driver created");
		return null;
	}

}
