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

package com.exactpro.remotehand.windows;

import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;

public class WindowsLoggingDriver<T extends WebElement> extends WindowsDriver<T> {

	private static final Logger logger = LoggerFactory.getLogger(WindowsLoggingDriver.class);
	
	public WindowsLoggingDriver(URL remoteAddress, Capabilities desiredCapabilities) {
		super(remoteAddress, desiredCapabilities);
	}

	@Override
	public Response execute(String driverCommand, Map<String, ?> parameters) {
		long startTime = System.currentTimeMillis();
		logger.info("Executing command: {}", driverCommand);
		try {
			return super.execute(driverCommand, parameters);
		} finally {
			logger.info("Command {} executed in {} millis", driverCommand, System.currentTimeMillis() - startTime);
		}
	}

	@Override
	public Response execute(String command) {
		long startTime = System.currentTimeMillis();
		logger.info("Executing command: {}", command);
		try {
			return super.execute(command);
		} finally {
			logger.info("Command {} executed in {} millis", command, System.currentTimeMillis() - startTime);
		}
	}
}
