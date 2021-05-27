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

package com.exactpro.remotehand.web.actions;

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class FindElement extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(FindElement.class);
	
	public static final String PARAM_ID = "id",
			RESULT_FOUND = "found",
			RESULT_NOTFOUND = "notfound";
	
	@Override
	public boolean isNeedLocator()
	{
		return true;
	}
	
	@Override
	public boolean isCanWait()
	{
		return false;  //Action implements the waiting logic by itself
	}

	@Override
	protected Logger getLogger()
	{
		return logger;
	}
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		int waitDuration;
		if ((params.containsKey(PARAM_WAIT)) && (!params.get(PARAM_WAIT).isEmpty()))
			waitDuration = getIntegerParam(params, PARAM_WAIT);
		else
			waitDuration = 0;
		
		String id = params.get(PARAM_ID);
		if (id == null)
			id = "";
		else if (!id.isEmpty())
			id += "=";
		
		String result;
		try
		{
			boolean found = waitForElement(webDriver, waitDuration, webLocator);
			result = (found) ? RESULT_FOUND : RESULT_NOTFOUND;
		}
		catch (ScriptExecuteException e)
		{
			result = RESULT_NOTFOUND;
		}
		return id+result;
	}
}
