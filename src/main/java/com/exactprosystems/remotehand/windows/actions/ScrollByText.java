/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.utils.SendKeysHandler;
import com.exactprosystems.remotehand.windows.ElementSearcher;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext.CachedWebElements;
import org.apache.commons.lang3.tuple.ImmutablePair;
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

		ElementSearcher searcher = new ElementSearcher(params, driverWrapper.getDriver(), cachedElements);
		WebElement expectedElement = searcher.searchElement();
		
		if (expectedElement.isDisplayed()) {
			logger.debug("Expected element is initially displayed");
			return null;
		}
		
		WebElement textControl = searcher.searchElement(new ImmutablePair<>("textlocator", "textmatcher"));
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
		Actions actions = new Actions(driverWrapper.getDriver());
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
