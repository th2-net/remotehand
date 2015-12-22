////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;
import com.exactprosystems.remotehand.actions.helpers.KeyCode;

public class SendKeys extends WebAction
{
	private static final Logger logger = Logger.getLogger(SendKeys.class);
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
				CharSequence k = getKeysByLevel(s.substring(1));
				if (k != null)
					input.sendKeys(k);
			}
		logger.info("Sent text to: " + webLocator);

		return null;
	}

	public CharSequence getKeysByLevel(String label)
	{
		switch (KeyCode.codeByLabel(label))
		{
			case UP:        return Keys.UP;
			case DOWN:      return Keys.DOWN;
			case LEFT:      return Keys.LEFT;
			case RIGHT:     return Keys.RIGHT;
			case RETURN:    return Keys.RETURN;
			case SPACE:     return Keys.SPACE;
			case HASH:      return Keys.chord(Keys.SHIFT, "3");
			case DOLLAR:    return Keys.chord(Keys.SHIFT, "4");
			case PERCENT:   return Keys.chord(Keys.SHIFT, "5");
			default :       return null;
		}
	}
}
