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
import com.exactpro.remotehand.windows.ElementOffsetUtils;
import com.exactpro.remotehand.windows.ElementOffsetUtils.ElementOffsetParams;
import com.exactpro.remotehand.windows.ElementSearcher;
import com.exactpro.remotehand.windows.SearchParams;
import com.exactpro.remotehand.windows.WinActionUtils;
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DragAndDropElement extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(DragAndDropElement.class);
	
	public static final String FROM_OFFSET_X = "fromoffsetx";
	public static final String FROM_OFFSET_Y = "fromoffsety";
	public static final String TO_OFFSET_X = "tooffsetx";
	public static final String TO_OFFSET_Y = "tooffsety";
	public static final String TO_PREFIX = "to";
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException {

		WindowsDriver<?> driver = getDriver(driverWrapper);
		ElementSearcher searcher = new ElementSearcher(params, driver, cachedElements);
		WebElement fromElement = searcher.searchElement();
		WebElement toElement = searcher.searchElement(new SearchParams.HeaderKeys(TO_PREFIX));

		ElementOffsetUtils.ElementOffsets fromOffsets = ElementOffsetUtils.calculateOffset(
				new ElementOffsetParams(fromElement, params.get(FROM_OFFSET_X), params.get(FROM_OFFSET_Y)));
		ElementOffsetUtils.ElementOffsets toOffsets = ElementOffsetUtils.calculateOffset(
				new ElementOffsetParams(toElement, params.get(TO_OFFSET_X), params.get(TO_OFFSET_Y)));

		Actions actions = WinActionUtils.createActionsAndCheck(driver, fromElement, toElement);
		if (fromOffsets.hasOffset) {
			actions.moveToElement(fromOffsets.element, fromOffsets.xOffset, fromOffsets.yOffset);
		} else {
			actions.moveToElement(fromOffsets.element);
		}
		actions.clickAndHold();
		if (toOffsets.hasOffset) {
			actions.moveToElement(toOffsets.element, toOffsets.xOffset, toOffsets.yOffset);
		} else {
			actions.moveToElement(toOffsets.element);
		}
		actions.release();
		
		actions.perform();
		return null;
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
