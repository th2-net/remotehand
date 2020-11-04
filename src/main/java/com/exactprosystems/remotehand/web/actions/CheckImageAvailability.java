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

package com.exactprosystems.remotehand.web.actions;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class CheckImageAvailability extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(CheckImageAvailability.class);

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
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement image = findElement(webDriver, webLocator);
		Object result = ((JavascriptExecutor) webDriver).executeScript(
				"return arguments[0].complete && " +
						"typeof arguments[0].naturalWidth != \"undefined\" && " +
						"arguments[0].naturalWidth > 0", image);

		boolean loaded = false;
		if (result instanceof Boolean)
		{
			loaded = (Boolean) result;
		}
		return String.valueOf(loaded);
	}

	@Override
	protected Logger getLogger()
	{
		return logger;
	}
}
