/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		executeJsCommands(webDriver, splitCommands(getJsScript(params)), getJsArguments(webDriver, webLocator, params));
		return null;
	}

	protected List<String> splitCommands(String jsScript) throws ScriptExecuteException
	{
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

	protected void executeJsCommands(WebDriver webDriver, List<String> commands, Object... args)
			throws ScriptExecuteException
	{
		WebJsUtils.executeJsCommands(webDriver, commands, args);
	}

	protected Object[] getJsArguments(WebDriver webDriver, By webLocator, Map<String, String> params)
	{
		return null;
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
