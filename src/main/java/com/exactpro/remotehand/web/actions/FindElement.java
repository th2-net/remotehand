/*
 * Copyright 2020-2024 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand.web.actions;

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Map;

public class FindElement extends WebAction {
	public static final String PARAM_ID = "id";
	public static final String RESULT_FOUND = "found";
	public static final String RESULT_NOT_FOUND = "notfound";

	public FindElement() {
		super(true, false); // Action implements the waiting logic by itself
	}

	public FindElement(boolean locatorNeeded, boolean canWait, String... mandatoryParams) {
		super(locatorNeeded, canWait, mandatoryParams);
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		int waitDuration = params.containsKey(PARAM_WAIT) && !params.get(PARAM_WAIT).isEmpty()
				? getIntegerParam(params, PARAM_WAIT) : 0;

		String id = params.getOrDefault(PARAM_ID, "");
		if (!id.isEmpty())
			id += "=";

		boolean isFound;
		try {
			isFound = waitForElement(webDriver, waitDuration, webLocator);
		} catch (ScriptExecuteException e) {
			isFound = false;
		}

		return id + (isFound ? RESULT_FOUND : RESULT_NOT_FOUND);
	}
}