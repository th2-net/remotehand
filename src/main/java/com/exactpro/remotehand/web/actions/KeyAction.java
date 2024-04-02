/*
 * Copyright 2020-2024 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand.web.actions;

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebAction;
import com.exactpro.remotehand.web.utils.SendKeysHandler;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.Map;

public class KeyAction extends WebAction {
	private static final String PARAM_KEY = "key";
	private static final String PARAM_KEY_ACTION = "keyaction";
	private static final String ACTION_PRESS = "press";
	private final SendKeysHandler handler = new SendKeysHandler();

	public KeyAction() {
		super(false, false);
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		String keyAction = params.getOrDefault(PARAM_KEY_ACTION, ACTION_PRESS);
		ActionType actionType = getActionType(keyAction);

		String keyParam = params.get(PARAM_KEY);
		if (!StringUtils.isEmpty(keyParam)) {
			List<String> keys = handler.processInputText(keyParam);
			for (String key : keys) {
				performKeyAction(webDriver, getKey(key), actionType);
			}
		}

		return null;
	}

	protected CharSequence getKey(String s) throws ScriptExecuteException {
		if (s.length() < 2)
			return null;
		String name = s.substring(1);
		CharSequence key = handler.getKeysByLabel(name);
		if (key == null)
			throw new ScriptExecuteException("Unknown key: " + name);
		return key;
	}

	protected ActionType getActionType(String keyAction) throws ScriptExecuteException {
		try {
			return ActionType.valueOf(keyAction.toLowerCase());
		} catch (IllegalArgumentException e) {
			throw new ScriptExecuteException("Unknown key action: " + keyAction, e);
		}
	}

	protected void performKeyAction(WebDriver webDriver, CharSequence key, ActionType actionType)
			throws ScriptExecuteException {
		Actions actions = new Actions(webDriver);
		switch (actionType) {
			case press:
				actions.sendKeys(key).perform();
				break;
			case down:
				actions.keyDown(key).perform();
				break;
			case up:
				actions.keyUp(key).perform();
				break;
			default:
				throw new ScriptExecuteException("Unknown action type: " + actionType);
		}
	}

	public enum ActionType { press, down, up }
}