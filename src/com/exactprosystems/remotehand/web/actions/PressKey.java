/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.exactprosystems.remotehand.web.actions.SendKeys.getKeysByLabel;
import static com.exactprosystems.remotehand.web.actions.SendKeys.processInputText;

public class PressKey extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(PressKey.class);
	
	private static final String PARAM_KEY = "key";

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		String keyParam = params.get(PARAM_KEY);
		if (!StringUtils.isEmpty(keyParam))
		{
			List<String> values = processInputText(keyParam);
			for (String s : values)
				pressKey(webDriver, s);
		}
		
		return null;
	}
	
	private void pressKey(WebDriver webDriver, String s) throws ScriptExecuteException
	{
		if (s.length() < 2)
			return;
		String name = s.substring(1);
		CharSequence key = getKeysByLabel(name);
		if (key == null)
			throw new ScriptExecuteException("Unknown key: " + name);
		((RemoteWebDriver) webDriver).getKeyboard().pressKey(key);
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
}
