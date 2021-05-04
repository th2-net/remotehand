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
import com.exactpro.remotehand.windows.WindowsSessionContext.CachedWebElements;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SearchElement extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(SearchElement.class);
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, CachedWebElements cachedWebElements) throws ScriptExecuteException {
		if (getId() == null) {
			throw new ScriptExecuteException("Id is not specified");
		}

		WindowsDriver<?> driver = getDriver(driverWrapper);
		ElementSearcher elementSearcher = new ElementSearcher(params, driver, cachedWebElements);
		WebElement webElement = elementSearcher.searchElement();
		cachedWebElements.storeWebElement(getId(), webElement);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Stored element to cached. RH_id: {} Win_id: {}", getId(), 
					(webElement instanceof RemoteWebElement) ? ((RemoteWebElement) webElement).getId() : "");
		}
		
		return null;
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
