////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import com.exactprosystems.remotehand.web.WebScriptCompiler;
import com.exactprosystems.remotehand.web.webelements.WebLocatorsMapping;

public class SendKeys extends WebAction
{
	private static final Logger logger = Logger.getLogger(SendKeys.class);
	private static final String PARAM_TEXT = "text",
			PARAM_TEXT2 = PARAM_TEXT+"2",
			PARAM_WAIT2 = PARAM_WAIT+"2",
			PARAM_LOCATOR2 = WebScriptCompiler.WEB_LOCATOR+"2",
			PARAM_MATCHER2 = WebScriptCompiler.WEB_MATCHER+"2",
			KEY_SIGN = "#",
			CLEAR_BEFORE = "clear";

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
		WebElement input = findElement(webDriver, webLocator);

		String beforeClear = params.get(CLEAR_BEFORE);
		if (beforeClear != null && (beforeClear.equalsIgnoreCase("yes") || beforeClear.equalsIgnoreCase("true"))) {
			input.clear();
			logger.debug("Text field has been cleared.");
		}

		String text = params.get(PARAM_TEXT);
		text = replaceConversions(text);

		sendText(input, text);
		logger.info("Sent text to: " + webLocator);


		if (!params.containsKey(PARAM_TEXT2))
			return null;

		String text2 = params.get(PARAM_TEXT2);
		text2 = replaceConversions(text2);

		boolean needRun = true;
		if ((params.containsKey(PARAM_WAIT2)) && (!params.get(PARAM_WAIT2).isEmpty()))
		{
			int wait2 = getIntegerParam(params, PARAM_WAIT2);
			if ((params.containsKey(PARAM_LOCATOR2)) && (params.containsKey(PARAM_MATCHER2)))
			{
				try
				{
					By locator2 = WebLocatorsMapping.getInstance().getByName(params.get(PARAM_LOCATOR2)).getWebLocator(webDriver, params.get(PARAM_MATCHER2));
					if (!waitForElement(wait2, locator2))
						needRun = false;
				}
				catch (ScriptCompileException e)
				{
					throw new ScriptExecuteException("Error while resolving locator2", e);
				}
			}
			else
				Wait.webWait(webDriver, wait2);
		}

		if (needRun)
		{
			sendText(input, text2);
			logger.info("Sent text2 to: " + webLocator);
		}

		return null;
	}

	protected void sendText(WebElement input, String text)
	{
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

	private String replaceConversions(String src) {
		return src.replace("(","#openbracket#");
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
		put("openbracket", Keys.chord(Keys.SHIFT, "9"));
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
