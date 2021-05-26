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

package com.exactpro.remotehand.windows.actions;

import com.exactpro.remotehand.RhUtils;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.windows.ElementSearcher;
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CheckElement extends WindowsAction {
	
	public static final String ATTRIBUTE_NAME = "attributename";
	public static final String SAVE_ELEMENT_NAME = "saveelement";
	
	public static final String FOUND_VALUE = "found";
	public static final String NOT_FOUND_VALUE = "not found";

	private static final Logger loggerInstance = LoggerFactory.getLogger(CheckElement.class);

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {

		ElementSearcher es = new ElementSearcher(params, this.getDriver(driverWrapper), cachedWebElements);
		WebElement element = es.searchElementWithoutWait();

		String attributeName = params.get(ATTRIBUTE_NAME);
		if (attributeName != null && element != null) {
			return element.getAttribute(attributeName);
		} else if (element != null) {
			String id = getId();
			if (RhUtils.getBooleanOrDefault(params, SAVE_ELEMENT_NAME, false) &&
					StringUtils.isNotEmpty(id)) {
				cachedWebElements.storeWebElement(id, element);
			}
			
			return FOUND_VALUE;
		}

		return NOT_FOUND_VALUE;
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
