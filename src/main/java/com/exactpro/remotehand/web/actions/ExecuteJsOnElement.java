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
 
package com.exactpro.remotehand.web.actions;

import com.exactpro.remotehand.ScriptExecuteException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ExecuteJsOnElement extends ExecuteJS
{
	private static final Logger logger = LoggerFactory.getLogger(ExecuteJsOnElement.class);

	protected static final String ELEMENT_PARAM = "@Element@";
	protected static final String ARGUMENT = "arguments[0]";

	@Override
	protected String getJsScript(Map<String, String> params) throws ScriptExecuteException
	{
		String jsCommands = super.getJsScript(params);
		return jsCommands.replace(ELEMENT_PARAM, ARGUMENT);
	}

	@Override
	protected Object[] getJsArguments(WebDriver webDriver, By webLocator, Map<String, String> params)
	{
		WebElement element = findElement(webDriver, webLocator);
		return new Object[] { element };
	}


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
	protected Logger getLogger()
	{
		return logger;
	}
}
