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

import com.exactpro.remotehand.Configuration;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.screenwriter.DefaultScreenWriter;
import com.exactpro.remotehand.screenwriter.ScreenWriter;
import com.exactpro.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.file.Path;
import java.util.Map;

public class StoreElementState extends WebAction {
	private static final String PARAM_ID = "id";
	private static final String[] MANDATORY_PARAMS = { PARAM_ID };
	private static final String SCREENSHOT_NAME = "takeScreenshotAction";
	private static final ScreenWriter<?> SCREEN_WRITER = new DefaultScreenWriter();

	public StoreElementState() {
		super(true, true, MANDATORY_PARAMS);
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		WebElement element = findElement(webDriver, webLocator);
		String fileName = SCREEN_WRITER.takeAndSaveElementScreenshot(SCREENSHOT_NAME, webDriver, element);
		Path screenshotPath = Configuration.SCREENSHOTS_DIR_PATH.resolve(fileName).toAbsolutePath();
		context.getContextData().put(buildScreenshotId(params.get(PARAM_ID)), screenshotPath);

		return null;
	}

	public static String buildScreenshotId(String id) {
		return "Screenshot_" + id;
	}
}