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

package com.exactpro.remotehand.web;

import com.exactpro.remotehand.ScriptExecuteException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WebJsUtils
{
	private static final Logger logger = LoggerFactory.getLogger(WebJsUtils.class);


	public static List<Object> executeJsCommands(WebDriver webDriver, Collection<String> commands, Object... args)
			throws ScriptExecuteException
	{
		if (!(webDriver instanceof JavascriptExecutor))
			throw new ScriptExecuteException("Web driver is not JavaScript executor: JS commands cannot be executed");
		
		JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
		List<Object> jsResults = new ArrayList<>();
		for (String command : commands)
		{
			Object result = executeJsCommand(jsExecutor, command, args);
			if (result != null) {
				jsResults.add(result);
			}
		}
		return jsResults;
	}

	private static Object executeJsCommand(JavascriptExecutor jsExecutor, String javaScript, Object... args)
			throws ScriptExecuteException
	{
		try
		{
			logger.info("Executing JS command: {}", javaScript);
			Object res = jsExecutor.executeScript(javaScript, args);
			if (res != null)
				logger.info("Result of JS command: {} = {}", javaScript, res);
			return res;
		}
		catch (Exception e)
		{
			throw new ScriptExecuteException("JS command cannot be executed: " + javaScript, e);
		}
	}
}