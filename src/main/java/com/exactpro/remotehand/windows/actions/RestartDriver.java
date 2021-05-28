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
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class RestartDriver extends WindowsAction {

	private static final Logger logger = LoggerFactory.getLogger(RestartDriver.class);
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException {
		boolean fromRoot = this.getBoolean(params, FROM_ROOT_PARAM);
		boolean experimental = this.getBoolean(params, EXPERIMENTAL_PARAM);
		driverWrapper.restartDriver(fromRoot, experimental);
		return null;
	}
	
	private boolean getBoolean(Map<String, String> params, String key) throws ScriptExecuteException {
		String param = params.get(key);
		if (param == null || param.isEmpty()) {
			throw new ScriptExecuteException("Param should be specified : " + key);
		}
		if (RhUtils.YES.contains(param)) {
			return true;
		} else if (RhUtils.NO.contains(param)) {
			return false;
		} else {
			throw new ScriptExecuteException(String.format("Invalid value for param %s. boolean required (actual: %s", 
					key, param));
		}
	}

	@Override
	protected Logger getLoggerInstance() {
		return logger;
	}
}
