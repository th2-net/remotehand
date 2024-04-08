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

package com.exactpro.remotehand.web;

import com.exactpro.remotehand.ScriptCompileException;
import com.exactpro.remotehand.web.webelements.WebLocator;

import java.util.Map;

public class WebScriptChecker
{
	public void checkParams(WebAction action, WebLocator webLocator, Map<String, String> params) throws ScriptCompileException
	{
		if (action.isLocatorNeeded() && webLocator == null)
			throw new ScriptCompileException("Mandatory parameter '" + WebScriptCompiler.WEB_LOCATOR + "' for action '" + action.getClass().getSimpleName() + "' is not set.");

		final String[] mandatoryParams = action.getMandatoryParams();
		if (mandatoryParams!=null)
			for (String mandatoryParam : mandatoryParams)
			{
				if (params.get(mandatoryParam) == null)
					throw new ScriptCompileException("Mandatory parameter '" + mandatoryParam + "' for action '" + action.getClass().getSimpleName() + "' is not set.");
			}
	}

	public void checkParams(WebLocator locator, Map<String, String> params) throws ScriptCompileException
	{
		if (locator == null)
			return;

		final String[] mandatoryParams = locator.getMandatoryParams();

		for (String mandatoryParam : mandatoryParams)
		{
			if (params.get(mandatoryParam) == null)
				throw new ScriptCompileException("Mandatory parameter '" + mandatoryParam + "' for locator '" + locator.getClass().getSimpleName() + "' is not set.");
		}
	}
}
