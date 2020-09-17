/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web.utils;

import com.exactprosystems.remotehand.SpecialKeys;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendKeysHandler {

	private static final Logger logger = LoggerFactory.getLogger(SendKeysHandler.class);

	public static final String SHIFT = "shift", CTRL = "ctrl", ALT = "alt", KEY_SIGN = "#", HASH = "#hash";

	public static final Map<String, CharSequence> KEYS = new HashMap<String, CharSequence>() {{
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
		put(SHIFT, Keys.SHIFT);
		put(CTRL, Keys.CONTROL);
		put(ALT, Keys.ALT);
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
		put("nbsp", SpecialKeys.NON_BREAKING_SPACE);
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
		put("subtract", Keys.SUBTRACT);
		put("add", Keys.ADD);
	}};

	public List<String> processInputText(String text)
	{
		if (StringUtils.isEmpty(text))
			return Collections.emptyList();

		List<String> strings = new ArrayList<>();
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
			{
				strings.add(s);
			}
		}
		return strings;
	}

	protected boolean isSpecialKey(String s)
	{
		if (StringUtils.isEmpty(s))
			return false;
		int plusIndex = s.indexOf('+');
		String firstKey = s.substring(1, plusIndex != -1 ? plusIndex : s.length()).toLowerCase();
		return KEYS.containsKey(firstKey);
	}
	
	public boolean needSpecialSend(String str) {
		return str.startsWith(KEY_SIGN);
	}

	public void sendSpecialKey(Actions a, String specKey, Object logElementIdentifier)
	{
		logger.trace("Sending key {}", specKey);
		CharSequence key = getKeysByLabel(specKey.substring(1));
		if (StringUtils.isNotEmpty(key))
		{
			if (logger.isTraceEnabled())
			{
				StringBuilder sb = new StringBuilder();
				for (int i = 0, len = key.length(); i < len; i++)
				{
					sb.append("\\u").append(Integer.toHexString(key.charAt(i))).append(" ");
				}
				logger.trace("Put to {} text {}", logElementIdentifier, sb);
			}
			doSendKeys(a, key);
		}
	}

	public void doSendKeys(Actions a, CharSequence keys)
	{
		a.sendKeys(keys);
		a.build().perform();
	}

	public CharSequence getKeysByLabel(String label)
	{
		if (!label.contains("+"))
			return KEYS.get(label.toLowerCase());

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

}
