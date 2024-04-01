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
import com.exactpro.remotehand.web.WebJsUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExecuteJS extends WebAction {
	private static final String JS_COMMANDS = "commands";
	private static final String NOTHING_TO_EXECUTE_MESSAGE = "Nothing to execute";
	private static final String DEFAULT_DELIMITER = ";";

	public ExecuteJS() {
		super(false, false);
	}

	public ExecuteJS(boolean locatorNeeded, boolean canWait, String... mandatoryParams) {
		super(locatorNeeded, canWait, mandatoryParams);
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		List<Object> jsResults = executeJsCommands(
				webDriver,
				splitCommands(getJsScript(params)),
				getJsArguments(webDriver, webLocator, params)
		);

		return jsResults == null || jsResults.isEmpty()
				? null
				: jsResults.stream().map(String::valueOf).collect(Collectors.joining(";"));
	}

	protected List<String> splitCommands(String jsScript) throws ScriptExecuteException {
		if (jsScript == null)
			return null;

		String[] splitScripts = jsScript.split(DEFAULT_DELIMITER);
		List<String> commands = new ArrayList<>();
		for (String splitScript : splitScripts) {
			if (!splitScript.isEmpty())
				commands.add(splitScript + DEFAULT_DELIMITER);
		}

		if (commands.isEmpty())
			throw new ScriptExecuteException(NOTHING_TO_EXECUTE_MESSAGE);

		return commands;
	}

	protected String getJsScript(Map<String, String> params) throws ScriptExecuteException {
		String jsScript = params.get(JS_COMMANDS);

		if (StringUtils.isEmpty(jsScript))
			throw new ScriptExecuteException(NOTHING_TO_EXECUTE_MESSAGE);

		return jsScript;
	}

	protected List<Object> executeJsCommands(
			WebDriver webDriver,
			List<String> commands,
			Object... args
	) throws ScriptExecuteException {
		if (commands == null) {
			throw new ScriptExecuteException("Commands to execute are not presented");
		}

		if (args == null) {
			throw new ScriptExecuteException("JS arguments should be presented");
		}

		return WebJsUtils.executeJsCommands(webDriver, commands, args);
	}

	private static final Object[] EMPTY_ARGS = new Object[0];
	protected Object[] getJsArguments(WebDriver webDriver, By webLocator, Map<String, String> params) {
		return EMPTY_ARGS;
	}
}