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
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WaitForElement extends WindowsAction {
	private static final Logger loggerInstance = LoggerFactory.getLogger(WaitForElement.class);

	private static final String FROM_ROOT_PARAM = "fromroot";
	private static final String TIMEOUT_PARAM = "timeout";

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params,
	                  WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException {
		String timeoutString = params.get(TIMEOUT_PARAM);
		if (StringUtils.isEmpty(timeoutString)) {
			throw new ScriptExecuteException("Timeout cannot be empty");
		}

		int timeout = Integer.parseInt(timeoutString);
		ElementSearcher elementSearcher = new ElementSearcher(params, this.getDriver(driverWrapper), cachedElements);
		WebElement element = elementSearcher.searchElementWithWait(timeout);

		if (element == null) {
			throw new ScriptExecuteException("Expected element was not found");
		}

		return null;
	}


	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
	
}
