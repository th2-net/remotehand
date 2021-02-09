/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactprosystems.remotehand.web.utils;

import com.exactprosystems.remotehand.SpecialKeys;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SendKeysHandler {

	private static final Logger logger = LoggerFactory.getLogger(SendKeysHandler.class);

	public static final String SHIFT = "shift", CTRL = "ctrl", ALT = "alt", KEY_SIGN = "#", HASH = "#hash";

	public static final List<CharSequence> MODIFIER_KEYS = Arrays.asList(Keys.SHIFT, Keys.CONTROL, Keys.ALT,
			Keys.META, Keys.COMMAND, Keys.LEFT_ALT, Keys.LEFT_CONTROL, Keys.LEFT_SHIFT);

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
		put("pageup", Keys.PAGE_UP);
		put("pagedown", Keys.PAGE_DOWN);
		put("command", Keys.COMMAND);
		put("windows", Keys.COMMAND);
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
		logger.trace("Sending key: {}", specKey);
		if (specKey.contains("+"))
			sendSpecialKeysCombination(a, specKey, logElementIdentifier);
		else
			sendSingleSpecialKey(a, specKey, logElementIdentifier);
	}

	protected void sendSingleSpecialKey(Actions a, String specKey, Object logElementIdentifier)
	{
		CharSequence key = getKeysByLabel(specKey.substring(1));
		if (StringUtils.isNotEmpty(key))
		{
			if (logger.isTraceEnabled())
				logger.trace("Sending text '{}' to element '{}'", Integer.toHexString(key.charAt(0)), logElementIdentifier);
			doSendKeys(a, key);
		}
	}

	protected void sendSpecialKeysCombination(Actions a, String specKey, Object logElementIdentifier)
	{
		CharSequence[] keys = getKeysArrayByLabel(specKey.substring(1));
		if (keys.length != 0)
		{
			if (logger.isTraceEnabled())
			{
				StringBuilder sb = new StringBuilder();
				for (CharSequence key : keys)
				{
					sb.append("\\u").append(Integer.toHexString(key.charAt(0))).append(" ");
				}
				logger.trace("Sending text '{}' to element '{}'", sb, logElementIdentifier);
			}
			doSendKeysCombination(a, keys);
		}
	}

	public void doSendKeys(Actions a, CharSequence keys)
	{
		a.sendKeys(keys);
		a.build().perform();
	}

	public void doSendKeysCombination(Actions a, CharSequence[] keys)
	{
		List<CharSequence> modifierKeys = new LinkedList<>(), usualKeys = new LinkedList<>();
		for (CharSequence key : keys)
		{
			if (MODIFIER_KEYS.contains(key))
			{
				modifierKeys.add(key);
				a.keyDown(key);
			}
			else
			{
				usualKeys.add(key);
			}
		}
		a.sendKeys(usualKeys.toArray(new CharSequence[0]));
		for (CharSequence key : modifierKeys)
		{
			a.keyUp(key);
		}
		a.build().perform();
	}

	public CharSequence getKeysByLabel(String label)
	{
		if (!label.contains("+"))
			return KEYS.get(label.toLowerCase());
		return Keys.chord(getKeysArrayByLabel(label));
	}

	public CharSequence[] getKeysArrayByLabel(String label)
	{
		String[] src = label.split("\\+");
		CharSequence[] res = new CharSequence[src.length];
		for (int i = 0; i < src.length; i++)
		{
			CharSequence c = KEYS.get(src[i].toLowerCase());
			res[i] = c == null ? src[i] : c;
		}
		return res;
	}
}
