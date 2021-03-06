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
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Wait extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(Wait.class);

	private static final String PARAM_MILLIS = "millis";
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {
		int millis = Integer.parseInt(params.get(PARAM_MILLIS));
		this.logger.info("Pause for " + millis + " millis");
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			this.logger.error("Sleep interrupted", e);
		}

		return null;
	}

	@Override
	protected String[] mandatoryParams() {
		return new String[] { PARAM_MILLIS };
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
