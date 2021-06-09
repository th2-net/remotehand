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
import com.exactpro.remotehand.windows.SearchParams;
import com.exactpro.remotehand.windows.WinActionUtils;
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext.CachedWebElements;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ScrollByText extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(ScrollByText.class);

	public final SendKeysHandler handler = new SendKeysHandler();
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, CachedWebElements cachedElements) throws ScriptExecuteException {

		ElementSearcher searcher = new ElementSearcher(params, this.getDriver(driverWrapper), cachedElements);
		WebElement expectedElement = searcher.searchElement();
		
		if (expectedElement.isDisplayed()) {
			logger.debug("Expected element is initially displayed");
			return null;
		}
		
		WebElement textControl = searcher.searchElement(new SearchParams.HeaderKeys("text"));
		String textControlId = (textControl instanceof RemoteWebElement ? ((RemoteWebElement) textControl).getId() : "");
		String text = params.get("texttosend");
		String maxItStr = params.get("maxiterations");
		int maxIterations = -1;
		if (maxItStr != null && !maxItStr.isEmpty()) {
			maxIterations = Integer.parseInt(maxItStr);
		}
		List<String> list = handler.processInputText(text);

		int count = 0;
		boolean displayed;
		Actions actions = WinActionUtils.createAndCheck(this.getDriver(driverWrapper));
		do {
			for (String str : list) {
				if (handler.needSpecialSend(str)) {
					handler.sendSpecialKey(actions, str, textControlId);
				} else {
					handler.doSendKeys(actions, str);
				}
			}
			count++;
			logger.debug("Scrolled {} times", count);
		} while (!(displayed = expectedElement.isDisplayed()) && (maxIterations == -1 || count < maxIterations));
		
		if (!displayed) {
			throw new ScriptExecuteException("Cannot scroll to element. " + count + " iteration passed");
		}
		
		return null;
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
