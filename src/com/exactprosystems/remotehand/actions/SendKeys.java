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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;

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
	public boolean isCanSwitchPage()
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
				CharSequence k = getKeysByLabel(s.substring(1));
				if (k != null)
					input.sendKeys(k);
			}
		logger.info("Sent text to: " + webLocator);

		return null;
	}

	public CharSequence getKeysByLabel(String label)
	{
		if (label.contains("+"))
		{
			String[] src = label.split("\\+");
			int size = src.length;
			CharSequence[] res = new CharSequence[size];
			for (int i = 0; i < size; i++)
			{
				CharSequence c = KEYS.get(src[i].toLowerCase());
				res[i] = c == null ? src[i] : c;
			}
			return Keys.chord(res);
		}
		else
			return KEYS.get(label.toLowerCase());
	}

	public static Map<String, CharSequence> KEYS = new HashMap<String, CharSequence>() {{
		put("up", Keys.UP);
		put("down", Keys.DOWN);
		put("left", Keys.LEFT);
		put("right", Keys.RIGHT);
		put("return", Keys.RETURN);
		put("space", Keys.SPACE);
		put("hash", Keys.chord(Keys.SHIFT, "3"));
		put("dollar", Keys.chord(Keys.SHIFT, "4"));
		put("percent", Keys.chord(Keys.SHIFT, "5"));
		put("tab", Keys.TAB);
		put("enter", Keys.ENTER);
		put("shift", Keys.SHIFT);
		put("ctrl", Keys.CONTROL);
		put("alt", Keys.ALT);
		put("esc", Keys.ESCAPE);
		put("end", Keys.END);
		put("home", Keys.HOME);
		put("insert", Keys.INSERT);
		put("delete", Keys.DELETE);
		put("f1", Keys.F1);
		put("f2", Keys.F2);
		put("f3", Keys.F3);
		put("f4", Keys.F4);
		put("f5", Keys.F5);
		put("f6", Keys.F6);
		put("f7", Keys.F7);
		put("f8", Keys.F8);
		put("f9", Keys.F9);
		put("f10", Keys.F10);
		put("f11", Keys.F11);
		put("f12", Keys.F12);
	}};
}
