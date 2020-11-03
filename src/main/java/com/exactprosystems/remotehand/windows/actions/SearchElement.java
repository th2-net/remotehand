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

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.windows.ElementSearcher;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext.CachedWebElements;
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

		ElementSearcher elementSearcher = new ElementSearcher(params, driverWrapper.getDriver(), cachedWebElements);
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
