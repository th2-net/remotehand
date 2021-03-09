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

package com.exactprosystems.remotehand.web.actions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.exactprosystems.remotehand.utils.ScreenshotRegionUtils;
import com.exactprosystems.remotehand.web.WebConfiguration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class StoreElementState extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(StoreElementState.class);
	
	private static final String PARAM_ID = "id";
	
	@Override
	public boolean isNeedLocator()
	{
		return true;
	}
	
	@Override
	public boolean isCanWait()
	{
		return true;
	}
	
	@Override
	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return new String[] {PARAM_ID};
	}
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement element = findElement(webDriver, webLocator);
		byte[] screen = ScreenshotRegionUtils.takeElementScreenshot(webDriver, element);
		Path fileName = Paths.get(WebConfiguration.SCREENSHOTS_DIR_NAME, "takeScreenshotAction" + System.currentTimeMillis() + ".jpeg");
		try {
			Files.write(fileName, screen);
		} catch (IOException e) {
			throw new ScriptExecuteException("Cannot store file", e);
		}
		context.getContextData().put(buildScreenshotId(params.get(PARAM_ID)), fileName);
		return null;
	}
	
	@Override
	protected Logger getLogger()
	{
		return logger;
	}
	
	
	public static String buildScreenshotId(String id)
	{
		return "Screenshot_"+id;
	}
}