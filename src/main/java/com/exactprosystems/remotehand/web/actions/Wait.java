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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class Wait extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(Wait.class);
	private static final String PARAM_SECONDS = "seconds";
	
	public Wait()
	{
		super.mandatoryParams = new String[]{PARAM_SECONDS};
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
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		int secs = getIntegerParam(params, PARAM_SECONDS);
		logInfo("Pause for "+secs+" second(s)");
		webWait(webDriver, secs);

		return null;
	}
	
	public static void webWait(WebDriver webDriver, int seconds)
	{
		try
		{
			(new WebDriverWait(webDriver, seconds)).until((new ExpectedCondition<Boolean>()
			{
				@Override
				public Boolean apply(WebDriver driver)
				{
					return false;
				}
			}));
		}
		catch (TimeoutException ex)
		{
			// Nothing should happen, it's normal to have timeout here
		}
	}
}
