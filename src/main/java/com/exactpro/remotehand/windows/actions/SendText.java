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

package com.exactpro.remotehand.windows.actions;

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.utils.SendKeysHandler;
import com.exactpro.remotehand.windows.ElementSearcher;
import com.exactpro.remotehand.windows.WinActions;
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SendText extends WindowsAction {
	
	public static final String TEXT_PARAM = "text";

	private static final Logger loggerInstance = LoggerFactory.getLogger(SendText.class);

	public final SendKeysHandler handler = new SendKeysHandler();

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {

		WindowsDriver<?> driver = this.getDriver(driverWrapper);
		ElementSearcher es = new ElementSearcher(params, driver, cachedWebElements);
		String clearBeforeStr = params.get("clearbefore");
		String textToSend = params.get(TEXT_PARAM);
		boolean clearBefore = "y".equals(clearBeforeStr) || "true".equals(clearBeforeStr);
		List<String> inputCommands = handler.processInputText(textToSend);
		String directSend = params.get("directsend");
		WebElement element = null;
		
		if (StringUtils.isNotEmpty(directSend) && ("y".equals(directSend) || "true".equals(directSend))) {
			logger.debug("Direct sending to control.");
			if (!es.isLocatorsAvailable()) {
				throw new ScriptExecuteException("Locator/Matcher should be specified");
			}
			element = es.searchElement();
			sendDirectCommand(element, inputCommands);
		} else {
			Actions actions = WinActions.createAndCheck(driver, element);
			if (es.isLocatorsAvailable()) {
				element = es.searchElement();
				actions.moveToElement(element);
				actions.click();
			}

			if (clearBefore) {
				actions.sendKeys(Keys.CONTROL, "a", Keys.CONTROL, Keys.BACK_SPACE);
			}

			for (String inputCommand : inputCommands) {
				if (handler.needSpecialSend(inputCommand)) {
					handler.sendSpecialKey(actions, inputCommand, (element instanceof RemoteWebElement ?
							((RemoteWebElement) element).getId() : ""));
				} else {
					handler.doSendKeys(actions, inputCommand);
				}
			}
		}
		
		return null;
	}

	@Override
	public Logger getLoggerInstance() {
		return loggerInstance;
	}

	@Override
	protected String[] mandatoryParams() {
		return new String[] { TEXT_PARAM };
	}


	private void sendDirectCommand(WebElement element, List<String> inputCommands) {
		for (String inputCommand : inputCommands) {
			if (handler.needSpecialSend(inputCommand)) {
				String command = inputCommand.substring(1);
				if (inputCommand.contains("+")) {
					CharSequence[] commands = handler.getKeysArrayByLabel(command);
					element.sendKeys(Keys.chord(commands));
				} else {
					CharSequence specialCommand = SendKeysHandler.KEYS.get(inputCommand.substring(1));
					if (StringUtils.isNotEmpty(specialCommand))
						element.sendKeys(specialCommand);
				}
			} else {
				element.sendKeys(inputCommand);
			}
		}
	}
}
