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

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.windows.ElementSearcher;
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
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
		
		WindowsDriver<?> driver = null;
		try {

			ElementSearcher elementSearcher = new ElementSearcher(params, this.getDriver(driverWrapper), cachedElements);
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
