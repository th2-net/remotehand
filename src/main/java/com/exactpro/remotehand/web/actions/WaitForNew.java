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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class WaitForNew extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(WaitForNew.class);
	private static final String PARAM_SECONDS = "seconds", 
			PARAM_CHECKMILLIS = "checkmillis";
	
	public WaitForNew()
	{
		super.mandatoryParams = new String[]{PARAM_SECONDS, PARAM_CHECKMILLIS};
	}
	
	@Override
	public boolean isNeedLocator()
	{
		return true;
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
	public String run(WebDriver webDriver, final By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		final int seconds = getIntegerParam(params, PARAM_SECONDS), 
				checkMillis = getIntegerParam(params, PARAM_CHECKMILLIS);

		try
		{
			(new WebDriverWait(webDriver, seconds)).until(new ExpectedCondition<Boolean>()
			{
				List<WebElement> previousElements = null;

				@Override
				public Boolean apply(WebDriver driver)
				{
					List<WebElement> elements = driver.findElements(webLocator);

					boolean foundEquals = false;
					if (previousElements != null)
					{
						foundEquals = elements.equals(previousElements);

						if (!foundEquals)
							try
							{
								Thread.sleep(checkMillis);
							}
							catch (InterruptedException e)
							{
								// do nothing
							}
					}

					previousElements = elements;

					return foundEquals;
				}
			});

			logInfo("Appeared locator: '%s'.", webLocator);
		}
		catch (TimeoutException ex)
		{
			throw new ScriptExecuteException("Timed out after " + seconds + " seconds waiting for '" + webLocator.toString() + "'");
		}

		return null;
	}
}
