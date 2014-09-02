package com.exactprosystems.remotehand;

import java.util.Map;

import com.exactprosystems.remotehand.webelements.WebLocator;

public class ScriptChecker
{

	public void checkParams(WebAction action, WebLocator webLocator, Map<String, String> params) throws ScriptCompileException
	{
		final String[] mandatoryParams = action.getMandatoryParams();

		if (action.isNeedLocator() && webLocator == null)
			throw new ScriptCompileException("Mandatory parameter '" + ScriptCompiler.WEB_LOCATOR + "' for action '" + action.getClass().getSimpleName() + "' is not set.");

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
