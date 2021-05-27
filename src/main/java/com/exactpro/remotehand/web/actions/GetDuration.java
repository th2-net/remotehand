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

public class GetDuration extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(GetDuration.class);
	protected static final String PARAM_STARTID = "startid",
			PARAM_NAME = "name",
			CONTEXT_LAST_GET_DURATION = "LastGetDuration";
	
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
	
	@Override
	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return new String[] {PARAM_NAME, PARAM_STARTID};
	}
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		Long start = null,
				end = System.currentTimeMillis();
		
		String id = params.get(PARAM_STARTID);
		start = (Long)context.getContextData().get(DurationStart.buildDurationStartId(id));
		if (start == null)
			throw new ScriptExecuteException("No 'DurationStart' action executed with ID='"+id+"'");
		
		return "Duration "+params.get(PARAM_NAME)+": "+(end-start);
	}
}
