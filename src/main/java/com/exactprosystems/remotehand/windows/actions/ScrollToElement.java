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

package com.exactprosystems.remotehand.windows.actions;

import com.exactprosystems.remotehand.RhUtils;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.utils.SendKeysHandler;
import com.exactprosystems.remotehand.windows.ElementOffsetUtils;
import com.exactprosystems.remotehand.windows.ElementSearcher;
import com.exactprosystems.remotehand.windows.SearchParams;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ScrollToElement extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(ScrollToElement.class);
	
	public static String ELEMENT_IN_TREE_PARAM = "elementindom"; // Element is in DOM
	public static String ELEMENT_SHOULD_BE_DISPLAYED = "shouldbedisplayed"; //Element should be displayed
	public static String SCROLL_TYPE_PARAM = "scrolltype"; // Scroll type
	public static String MAX_ITERATION_PARAM = "maxiterations"; // Max iterations
	
	public static String CLICK_OFFSET_X_PARAM = "clickoffsetx"; //Click Offset X
	public static String CLICK_OFFSET_Y_PARAM = "clockoffsety"; //Click Offset Y
	public static String TEXT_VALUE_PARAM = "textvalue"; // Text Value

	private ElementSearcher elementSearcher;
	public final SendKeysHandler handler = new SendKeysHandler();
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException {
		
		this.elementSearcher = new ElementSearcher(params, driverWrapper.getDriver(), cachedElements);
		boolean elementInTree = RhUtils.getBooleanOrDefault(params, ELEMENT_IN_TREE_PARAM, true);
		boolean elementShouldDisplayed = RhUtils.getBooleanOrDefault(params, ELEMENT_SHOULD_BE_DISPLAYED, true);
		int maxIterations = RhUtils.getIntegerOrDefault(params, MAX_ITERATION_PARAM, 10);
		String scrollType = params.getOrDefault(SCROLL_TYPE_PARAM, "");
		SearchParams.HeaderKeys actionKeys = new SearchParams.HeaderKeys("action");

		ScrollPerformer scrollPerformer = null;
		switch (scrollType) {
			case "Click": scrollPerformer = this.clickPerformer(driverWrapper.getDriver(), params, actionKeys); break;
			case "Text": scrollPerformer = this.textPerformer(driverWrapper.getDriver(), params, actionKeys); break;
			default: throw new ScriptExecuteException("Unknown scroll type");
		}
		
		WebElWrapper targetElement = new WebElWrapper();
		boolean displayed;
		int iteration = 0;
		
		while (!(displayed = elementDisplayed(elementInTree, elementShouldDisplayed, targetElement))
				&& iteration < maxIterations) {
			scrollPerformer.perform();
			iteration++;
		}
		
		if (!displayed) {
			throw new ScriptExecuteException("Cannot scroll to element. " + iteration + " iterations performed");
		}
		
		return null;
	}
	
	private boolean elementDisplayed(boolean elementInTree, boolean elementShouldDisplayed, WebElWrapper element)
			throws ScriptExecuteException {
		if (element.element == null) {
			if (elementInTree) {
				element.element = elementSearcher.searchElement();
			} else {
				element.element = elementSearcher.searchElementWithoutWait();
			}
		}
		return element.element != null && (!elementShouldDisplayed || element.element.isDisplayed());
	}
	
	private ScrollPerformer clickPerformer(WindowsDriver<?> driver, Map<String, String> params,
										   SearchParams.HeaderKeys keys) throws ScriptExecuteException {
		final WebElement element = elementSearcher.searchElement(keys);
		ElementOffsetUtils.ElementOffsetParams elementOffsetParams = new ElementOffsetUtils.ElementOffsetParams(element,
				params.get(CLICK_OFFSET_X_PARAM), params.get(CLICK_OFFSET_Y_PARAM));
		final ElementOffsetUtils.ElementOffsets elementOffsets = ElementOffsetUtils.calculateOffset(elementOffsetParams);;
		final Actions actions = new Actions(driver);
		return () -> {
			if (elementOffsets.hasOffset) {
				actions.moveToElement(element, elementOffsets.xOffset, elementOffsets.yOffset);
			} else {
				actions.moveToElement(element);
			}
			actions.click().perform();
		};
	}

	private ScrollPerformer textPerformer(WindowsDriver<?> driver, Map<String, String> params,
										  SearchParams.HeaderKeys keys) throws ScriptExecuteException {
		String textValue = params.get(TEXT_VALUE_PARAM);
		if (StringUtils.isEmpty(textValue)) {
			throw new ScriptExecuteException(TEXT_VALUE_PARAM + " should be present");
		}
		
		final List<String> list = handler.processInputText(textValue);
				
		final Actions actions = new Actions(driver);
		WebElement element = elementSearcher.searchElement(keys);
		actions.moveToElement(element).click();
		final String textControlId = ((RemoteWebElement) element).getId();
		
		return () -> {
			for (String str : list) {
				if (handler.needSpecialSend(str)) {
					handler.sendSpecialKey(actions, str, textControlId);
				} else {
					handler.doSendKeys(actions, str);
				}
			}
		};
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
	
	private interface ScrollPerformer {
		void perform() throws ScriptExecuteException;
	}
	
	private static class WebElWrapper {
		public WebElement element;
	}
}
