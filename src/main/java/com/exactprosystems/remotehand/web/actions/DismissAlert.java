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

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.exactprosystems.remotehand.web.WebUtils.waitForAlert;

public class DismissAlert extends WebAction
{
	private static final Logger log = LoggerFactory.getLogger(DismissAlert.class);
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		int wait = getIntegerParam(params, PARAM_WAIT);
		Alert alert = waitForAlert(webDriver, wait);
		try
		{
			alert.dismiss();
		}
		catch (Exception e)
		{
			throw new ScriptExecuteException("Unable to dismiss alert.", e);
		}
		return null;
	}

	@Override
	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return new String[]{PARAM_WAIT};
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
		return log;
	}
}
