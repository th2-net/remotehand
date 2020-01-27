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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.exactprosystems.remotehand.web.actions.SendKeys.getKeysByLabel;
import static com.exactprosystems.remotehand.web.actions.SendKeys.processInputText;

public class KeyAction extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(KeyAction.class);
	
	private static final String PARAM_KEY = "key", PARAM_KEYACTION = "keyaction";

	private static final String ACTION_PRESS = "press";

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		String keyAction = params.getOrDefault(PARAM_KEYACTION, ACTION_PRESS);
		ActionType actionType = getActionType(keyAction);

		String keyParam = params.get(PARAM_KEY);
		if (!StringUtils.isEmpty(keyParam))
		{
			List<String> keys = processInputText(keyParam);
			for (String key : keys)
			{
				performKeyAction(webDriver, getKey(key), actionType);
			}
		}
		
		return null;
	}
	
	protected CharSequence getKey(String s) throws ScriptExecuteException
	{
		if (s.length() < 2)
			return null;
		String name = s.substring(1);
		CharSequence key = getKeysByLabel(name);
		if (key == null)
			throw new ScriptExecuteException("Unknown key: " + name);
		return key;
	}

	protected ActionType getActionType(String keyAction) throws ScriptExecuteException
	{
		try
		{
			return ActionType.valueOf(keyAction.toLowerCase());
		}
		catch (IllegalArgumentException e)
		{
			throw new ScriptExecuteException("Unknown key action: " + keyAction, e);
		}
	}

	protected void performKeyAction(WebDriver webDriver, CharSequence key, ActionType actionType)
	{
		switch (actionType)
		{
			case press:
				pressKey(webDriver, key);
				break;
			case down:
				keyDown(webDriver, key);
				break;
			case up:
				keyUp(webDriver, key);
				break;
		}
	}

	protected void pressKey(WebDriver webDriver, CharSequence key)
	{
		((RemoteWebDriver) webDriver).getKeyboard().pressKey(key);
	}

	protected void keyDown(WebDriver webDriver, CharSequence key)
	{
		Actions actions = new Actions(webDriver);
		actions.keyDown(key).perform();
	}

	protected void keyUp(WebDriver webDriver, CharSequence key)
	{
		Actions actions = new Actions(webDriver);
		actions.keyUp(key).perform();
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
	
	public enum ActionType
	{
		press,
		down,
		up
	}
}
