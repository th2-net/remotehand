/*
 * Copyright 2020-2021 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand.windows.actions;

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.utils.SendKeysHandler;
import com.exactpro.remotehand.windows.*;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Click extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(Click.class);

	private static final String LEFT = "left", RIGHT = "right", MIDDLE = "middle", DOUBLE="double", BUTTON = "button",
			X_OFFSET = "xoffset", Y_OFFSET = "yoffset", HOLD_KEY_DOWN = "holdkeydown";

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {
		WindowsDriver<?> driver = getDriver(driverWrapper);
		ElementSearcher es = new ElementSearcher(params, driver, cachedWebElements);
		WebElement element = es.searchElement();

		String button = params.get(BUTTON);
		if (StringUtils.isEmpty(button))
			button = LEFT;

		ElementOffsetUtils.ElementOffsetParams elementOffsetParams 
				= new ElementOffsetUtils.ElementOffsetParams(element, params.get(X_OFFSET), params.get(Y_OFFSET));
		ElementOffsetUtils.ElementOffsets elementOffsets = ElementOffsetUtils.calculateOffset(elementOffsetParams);

		Actions actions = WinActionUtils.createActionsAndCheck(driver, element);

		if (elementOffsets.hasOffset) {
			actions = actions.moveToElement(element, elementOffsets.xOffset, elementOffsets.yOffset);
		} else {
			actions = actions.moveToElement(element);
		}

		List<CharSequence> holdKeysDown = extractKeys(params.get(HOLD_KEY_DOWN));
		processHoldKeys(actions::keyDown, holdKeysDown);

		switch (button) {
			case LEFT:
				actions.click();
				break;
			case RIGHT:
				actions.contextClick();
				break;
			case MIDDLE:
				throw new ScriptExecuteException("Middle click is not implemented.");
			case DOUBLE:
				actions.doubleClick();
				break;
			default:
				throw new ScriptExecuteException("Button may be only left, right, middle or double (for double click with left button).");
		}

		processHoldKeys(actions::keyUp, holdKeysDown);

		actions.perform();
		
		return null;
	}

	@Override
	public Logger getLoggerInstance() {
		return loggerInstance;
	}


	protected void processHoldKeys(Consumer<CharSequence> action, List<CharSequence> holdKeysDown) {
		if (holdKeysDown.isEmpty())
			return;

		holdKeysDown.forEach(action);
	}


	private List<CharSequence> extractKeys(String key) {
		if (key == null)
			return Collections.emptyList();

		String trimKey = key.trim();
		if (!trimKey.startsWith(SendKeysHandler.KEY_SIGN) && !trimKey.endsWith(SendKeysHandler.KEY_SIGN))
			return Collections.emptyList();

		String[] splitKeys = trimKey.substring(1, trimKey.length() - 1).split("\\+");
		List<CharSequence> result = new ArrayList<>(splitKeys.length);
		for (String splitKey : splitKeys) {
			CharSequence keySequence = SendKeysHandler.KEYS.get(splitKey.toLowerCase());
			if (keySequence != null)
				result.add(keySequence);
		}

		return result;
	}
}
