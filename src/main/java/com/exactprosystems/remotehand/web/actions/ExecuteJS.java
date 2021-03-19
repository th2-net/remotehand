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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.exactprosystems.remotehand.web.WebJsUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class ExecuteJS extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(ExecuteJS.class);
	protected static final String JS_COMMANDS = "commands";
	protected static final String NOTHING_TO_EXECUTE_MESSAGE = "Nothing to execute";
	protected static final String DEFAULT_DELIMITER = ";";

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		List<Object> jsResults = executeJsCommands(webDriver, splitCommands(getJsScript(params)), getJsArguments(webDriver, webLocator, params));
		if (jsResults == null || jsResults.isEmpty()) {
			return null;
		} else {
			return jsResults.stream().map(String::valueOf).collect(Collectors.joining(";"));
		}
		
	}

	protected List<String> splitCommands(String jsScript) throws ScriptExecuteException
	{
		if (jsScript == null)
			return null;
		
		String[] splitScripts = jsScript.split(DEFAULT_DELIMITER);

		List<String> commands = new ArrayList<>();
		for (String splitScript : splitScripts)
		{
			if (splitScript.isEmpty())
				continue;
			commands.add(splitScript + DEFAULT_DELIMITER);
		}

		if (commands.isEmpty())
			throw new ScriptExecuteException(NOTHING_TO_EXECUTE_MESSAGE);
		return commands;
	}

	protected String getJsScript(Map<String, String> params) throws ScriptExecuteException
	{
		String jsScript = params.get(JS_COMMANDS);
		if (StringUtils.isEmpty(jsScript))
			throw new ScriptExecuteException(NOTHING_TO_EXECUTE_MESSAGE);
		return jsScript;
	}

	protected List<Object> executeJsCommands(WebDriver webDriver, List<String> commands, Object... args)
			throws ScriptExecuteException
	{
		if (commands == null) {
			throw new ScriptExecuteException("Commands to execute are not presented");
		}
		if (args == null) {
			throw new ScriptExecuteException("JS arguments should be presented");
		}
		return WebJsUtils.executeJsCommands(webDriver, commands, args);
	}

	protected Object[] getJsArguments(WebDriver webDriver, By webLocator, Map<String, String> params)
	{
		return new Object[0];
	}


	@Override
	public boolean isNeedLocator()
	{
		return false;
	}

	@Override
	public boolean isCanWait()
	{
		return false;
	}

	@Override
	protected Logger getLogger()
	{
		return logger;
	}
}
