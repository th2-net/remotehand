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

import com.exactpro.remotehand.ScriptCompileException;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DurationStart extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(DurationStart.class);
	protected static final String PARAM_ID = "id",
			CONTEXT_LAST_DURATION_START = "LastDurationStart";
	
	@Override
	protected Logger getLogger()
	{
		return logger;
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
	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return new String[] {PARAM_ID};
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		Map<String, Object> contextData = context.getContextData();
		Long start = System.currentTimeMillis();
		contextData.put(buildDurationStartId(params.get(PARAM_ID)), start);
		contextData.put(CONTEXT_LAST_DURATION_START, start);
		return null;
	}
	
	
	public static String buildDurationStartId(String id)
	{
		return "DurationStart_"+id;
	}
}
