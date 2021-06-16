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

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.windows.ElementSearcher;
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ToggleCheckBox extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(ToggleCheckBox.class);
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {

		ElementSearcher es = new ElementSearcher(params, this.getDriver(driverWrapper), cachedWebElements);
		WebElement element = es.searchElement();
		
		String expectedState = params.get("expectedstate");
		expectedState = expectedState != null ? expectedState.toLowerCase() : null;
		boolean checked;
		if ("checked".equals(expectedState) || "true".equals(expectedState)) {
			checked = true;
		} else if ("unchecked".equals(expectedState) || "false".equals(expectedState)) {
			checked = false;
		} else {
			throw new ScriptExecuteException("Unknown expectedState = " + expectedState);
		}

		String toggleState = element.getAttribute("Toggle.ToggleState");
		this.logger.debug("Element source toggle state : {}", toggleState);
		
		if (toggleState != null && (("on".equalsIgnoreCase(toggleState) || "1".equals(toggleState)) && !checked) 
				|| (("off".equalsIgnoreCase(toggleState) || "0".equals(toggleState) && checked))) {
			element.click();
			this.logger.debug("RH clicked on this checkbox");
		} else {
			this.logger.debug("None of actions should be provided");
		}
		
		return null;

	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
