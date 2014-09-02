package com.exactprosystems.remotehand;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public abstract class WebAction
{
	protected boolean needLocator = true;

	protected String[] mandatoryParams;

	public abstract String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException;

	public String[] getMandatoryParams() throws ScriptCompileException
	{
		if (mandatoryParams == null)
			return new String[0];

		return mandatoryParams;
	};

	public boolean isNeedLocator()
	{
		return needLocator;
	}
}
