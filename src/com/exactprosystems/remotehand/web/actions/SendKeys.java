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
import org.openqa.selenium.*;

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
			CLEAR_BEFORE = "clear",
			CAN_BE_DISABLED = "canbedisabled",
			HASH = "#hash";

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
		WebElement input;
		if (webLocator != null)
			input = findElement(webDriver, webLocator);
		else
			input = getWebDriver().switchTo().activeElement();
		
		boolean shouldBeEnabled = shouldBeEnabledAtFirst(input, params);
		try
		{
			if (shouldBeEnabled)
				enable(webDriver, input);
			
			String beforeClear = params.get(CLEAR_BEFORE);
			if (beforeClear != null && (beforeClear.equalsIgnoreCase("yes") || beforeClear.equalsIgnoreCase("true")))
			{
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
		}
		finally
		{
			if (shouldBeEnabled)
				disable(webDriver, input);
		}
		return null;
	}

	protected void sendText(WebElement input, String text)
	{
		List<String> strings = processInputText(text);
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
	
	protected static List<String> processInputText(String text)
	{
		List<String> strings = new ArrayList<String>();
		boolean afterKey = false;
		for (String s : text.split("(?=#)"))
		{
			if (s.startsWith(KEY_SIGN))
			{
				if (isSpecialKey(s))
				{
					strings.add(s);
					afterKey = true;
				}
				else 
				{
					if (afterKey)
						afterKey = false;
					else 
						strings.add(HASH);
					
					if (s.length() > 1)
						strings.add(s.substring(1));
				}
			}
			else 
				strings.add(s);
		}
		return strings;
	}
	
	protected static boolean isSpecialKey(String s)
	{
		if (s.length() == 0)
			return false;		
		int plusIndex = s.indexOf('+');
		String firstKey = s.substring(1, plusIndex != -1 ? plusIndex : s.length());
		return KEYS.containsKey(firstKey);
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
	
	protected boolean shouldBeEnabledAtFirst(WebElement element, Map<String, String> params)
	{
		String canBeDisabled = params.get(CAN_BE_DISABLED);
		return canBeDisabled != null && !element.isEnabled() && WebScriptCompiler.YES.contains(canBeDisabled.toLowerCase());
	}
	
	protected void enable(WebDriver driver, WebElement input)
	{
		logger.info("Try to enable element");
		((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('disabled')", input);
		logger.info("Now element is " + (input.isEnabled() ? "enabled" : "still disabled"));
	}
	
	protected void disable(WebDriver driver, WebElement input)
	{
		logger.info("Try to disable element");
		((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('disabled', '')", input);
		logger.info("Now element is " + (input.isEnabled() ? "still enabled" : "disabled"));
	}

	protected static String replaceConversions(String src) {
		return src.replace("(","#openbracket#");
	}

	public static Map<String, CharSequence> KEYS = new HashMap<String, CharSequence>() {{
		put("up", Keys.UP);
		put("down", Keys.DOWN);
		put("left", Keys.LEFT);
		put("right", Keys.RIGHT);
		put("return", Keys.RETURN);
		put("space", Keys.SPACE);
		put(HASH, Keys.chord(Keys.SHIFT, "3"));
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
		put("backspace", Keys.BACK_SPACE);
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
		put("nbsp", Keys.chord(Keys.ALT, Keys.NUMPAD0, Keys.NUMPAD1, Keys.NUMPAD6, Keys.NUMPAD0));
		put("num0", Keys.NUMPAD0);
		put("num1", Keys.NUMPAD1);
		put("num2", Keys.NUMPAD2);
		put("num3", Keys.NUMPAD3);
		put("num4", Keys.NUMPAD4);
		put("num5", Keys.NUMPAD5);
		put("num6", Keys.NUMPAD6);
		put("num7", Keys.NUMPAD7);
		put("num8", Keys.NUMPAD8);
		put("num9", Keys.NUMPAD9);
	}};
}
