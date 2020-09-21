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
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WaitForAttribute extends WindowsAction {
	
	public static final String ATTRIBUTE_NAME_PARAM = "attributename";
	public static final String EXPECTED_VALUE_PARAM = "expectedvalue";
	public static final String MAX_TIMEOUT_PARAM = "maxtimeout";
	public static final String CHECK_INTERVAL_PARAM = "checkinterval";
	
	public static final String PARAM_NOT_SET = "Parameter %s is not set";

	private static final Logger loggerInstance = LoggerFactory.getLogger(WaitForAttribute.class);
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException {	
		
		String attributeName = params.get(ATTRIBUTE_NAME_PARAM);
		String expectedValue = params.get(EXPECTED_VALUE_PARAM);
		String timeout = params.get(MAX_TIMEOUT_PARAM);
		String checkIntervalStr = params.getOrDefault(CHECK_INTERVAL_PARAM, "1000");
		boolean fromRoot = Boolean.parseBoolean(params.getOrDefault("fromroot", "false"));
		
		WindowsDriver<?> driver = null;
		try {
			if (fromRoot) {
				driver = driverWrapper.getOrCreateRootDriver();
			} else {
				driver = driverWrapper.getDriver();
			}

			ElementSearcher elementSearcher = new ElementSearcher(params, driver, cachedElements);
			WebElement element = elementSearcher.searchElement();

			if (StringUtils.isEmpty(attributeName)) {
				throw new ScriptExecuteException(String.format(PARAM_NOT_SET, ATTRIBUTE_NAME_PARAM));
			}

			if (StringUtils.isEmpty(expectedValue)) {
				throw new ScriptExecuteException(String.format(PARAM_NOT_SET, EXPECTED_VALUE_PARAM));
			}

			if (StringUtils.isEmpty(timeout)) {
				throw new ScriptExecuteException(String.format(PARAM_NOT_SET, MAX_TIMEOUT_PARAM));
			}

			long startTime = System.currentTimeMillis();
			long maxTimeout = Long.parseLong(timeout);
			long checkInterval = Long.parseLong(checkIntervalStr);
			long endTime = startTime + maxTimeout;

			boolean correctTimeout = maxTimeout > 0 && checkInterval > 0;
			boolean passed = false, firstRun = true;
			String attribute;

			do {
				if (!firstRun) {
					logger.debug("Waiting for {} ms", checkInterval);
					Thread.sleep(checkInterval);
				}
				firstRun = false;
				attribute = element.getAttribute(attributeName);
				logger.debug("Attribute {} = {}", attributeName, attribute);
				passed = expectedValue.equals(attribute);
			} while (correctTimeout && !passed && endTime > System.currentTimeMillis());

			if (!passed) {
				throw new ScriptExecuteException(String.format("Unexpected attribute received: %s. Expected: %s. ",
						attribute, expectedValue));
			}

		} catch (InterruptedException e) {
			throw new ScriptExecuteException("Waiting interrupted", e);			
		}	
		return null;		
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
