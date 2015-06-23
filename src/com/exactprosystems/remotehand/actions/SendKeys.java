////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.exactprosystems.remotehand.Logger;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;
import com.exactprosystems.remotehand.actions.helpers.KeyCode;

public class SendKeys extends WebAction
{
	private static final Logger logger = Logger.getLogger();
	private static final String PARAM_TEXT = "text",
			KEY_SIGN = "#";

	public SendKeys()
	{
		super.mandatoryParams = new String[]{PARAM_TEXT};
	}
	
	@Override
	public boolean isNeedLocator()
	{
		return true;
	}
	
	@Override
	public boolean isCanWait()
	{
		return true;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement input = webDriver.findElement(webLocator);
		String text = params.get(PARAM_TEXT);
		List<String> strings = new ArrayList<String>();
		int index;
		while ((index = text.indexOf(KEY_SIGN))>-1)
		{
			int endIndex = text.indexOf(KEY_SIGN, index+1);
			if (endIndex < 0)
				break;
			strings.add(text.substring(0, index));  //Text before non-text key
			strings.add(text.substring(index, endIndex));  //Non-text key code
			text = text.substring(endIndex+1);
		}
		if (!text.isEmpty())
			strings.add(text);
		
		for (String s : strings)
			if (!s.startsWith(KEY_SIGN))
				input.sendKeys(s);
			else
			{
				Keys k;
				switch (KeyCode.codeByLabel(s.substring(1)))
				{
				case UP : k = Keys.UP; break;
				case DOWN : k = Keys.DOWN; break;
				case LEFT : k = Keys.LEFT; break;
				case RIGHT : k = Keys.RIGHT; break;
				case RETURN : k = Keys.RETURN; break;
				default : k = null;
				}
				if (k != null)
					input.sendKeys(k);
			}
		logger.info("Sent text to: " + webLocator);

		return null;
	}
}
