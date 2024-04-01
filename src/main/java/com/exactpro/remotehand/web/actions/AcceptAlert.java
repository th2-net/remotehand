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
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Map;

import static com.exactpro.remotehand.web.WebUtils.waitForAlert;

public class AcceptAlert extends WebAction {
	public AcceptAlert() {
		super(false, false, PARAM_WAIT);
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		int wait = getIntegerParam(params, PARAM_WAIT);
		Alert alert = waitForAlert(webDriver, wait);
		try {
			alert.accept();
		} catch (Exception e) {
			throw new ScriptExecuteException("Unable to accept alert.", e);
		}
		return null;
	}
}