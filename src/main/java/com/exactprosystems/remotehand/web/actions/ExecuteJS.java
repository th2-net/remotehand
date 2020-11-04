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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

import static com.exactprosystems.remotehand.web.WebJsUtils.executeJsCommands;

public class ExecuteJS extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(ExecuteJS.class);
	private static final String JS_COMMANDS = "commands";
	private static final String NOTHING_TO_EXECUTE_MESSAGE = "Nothing to execute";
	protected static final String DEFAULT_DELIMITER = ";";


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
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		List<String> commands = getCommands(params);
		if (commands.isEmpty())
			throw new ScriptExecuteException(NOTHING_TO_EXECUTE_MESSAGE);

		executeJsCommands(webDriver, commands);

		return null;
	}


	@Override
	protected Logger getLogger()
	{
		return logger;
	}


	private List<String> getCommands(Map<String, String> params) throws ScriptExecuteException
	{
		String jsScripts = params.get(JS_COMMANDS);

		if (StringUtils.isEmpty(jsScripts))
			throw new ScriptExecuteException(NOTHING_TO_EXECUTE_MESSAGE);

		String[] splitScripts = jsScripts.split(DEFAULT_DELIMITER);

		List<String> result = new ArrayList<>(splitScripts.length);
		for (String splitScript : splitScripts)
		{
			if (splitScript.isEmpty())
				continue;

			result.add(splitScript + DEFAULT_DELIMITER);
		}

		return result;
	}
}
