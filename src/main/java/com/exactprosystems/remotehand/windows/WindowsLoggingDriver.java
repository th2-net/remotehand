/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows;

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
		Response response = super.execute(driverCommand, parameters);
		logger.info("Command {} executed in {} millis", driverCommand, System.currentTimeMillis() - startTime);
		return response;
	}

	@Override
	public Response execute(String command) {
		long startTime = System.currentTimeMillis();
		logger.info("Executing command: {}", command);
		Response response = super.execute(command);
		logger.info("Command {} executed in {} millis", command, System.currentTimeMillis() - startTime);
		return response;
	}
}
