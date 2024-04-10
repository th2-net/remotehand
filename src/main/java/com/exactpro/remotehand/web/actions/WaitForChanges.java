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
import com.exactpro.remotehand.screenwriter.DefaultScreenWriter;
import com.exactpro.remotehand.screenwriter.ScreenWriter;
import com.exactpro.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WaitForChanges extends WebAction {
	private static final String PARAM_SECONDS = "seconds";
	private static final String PARAM_SCREENSHOT_ID = "screenshotid";
	private static final String PARAM_CHECK_MILLIS = "checkmillis";
	private static final String[] MANDATORY_PARAMS = { PARAM_SECONDS, PARAM_SCREENSHOT_ID, PARAM_CHECK_MILLIS };
	private static final ScreenWriter<?> screenWriter = new DefaultScreenWriter();

	public WaitForChanges() {
		super(true, false, MANDATORY_PARAMS);
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		int seconds = getIntegerParam(params, PARAM_SECONDS);
		int checkMillis = getIntegerParam(params, PARAM_CHECK_MILLIS);
		String id = params.get(PARAM_SCREENSHOT_ID);

		Path screenPath = (Path)context.getContextData().get(StoreElementState.buildScreenshotId(id));
		if (screenPath == null)
			throw new ScriptExecuteException("No screenshot stored for ID='" + id + "'");

		byte[] initialState;
		try {
			initialState = Files.readAllBytes(screenPath);
		} catch (Exception e) {
			throw new ScriptExecuteException("Error retrieving saved screenshot for ID='" + id + "'");
		}

		long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds);
		do {
			WebElement element = findElement(webDriver, webLocator);
			byte[] currentState = screenWriter.takeElementScreenshot(webDriver, element);
			if (!compareStates(initialState, currentState))
				return null;
			
			if (System.currentTimeMillis() >= endTime)
				break;
			
			try {
				Thread.sleep(checkMillis);
			} catch (InterruptedException e) {
				// do nothing like in WaitForNew
			}
		} while (true);

		throw new ScriptExecuteException("No changes caught in element during " + seconds + " seconds");
	}

	private boolean compareStates(byte[] state1, byte[] state2) {
		return Arrays.equals(state1, state2);
	}
}