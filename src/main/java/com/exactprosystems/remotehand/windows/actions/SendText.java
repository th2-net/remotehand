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
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SendText extends WindowsAction {
	
	public static final String TEXT_PARAM = "text";

	private static final Logger loggerInstance = LoggerFactory.getLogger(SendText.class);

	public final SendKeysHandler handler = new SendKeysHandler();

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {

		ElementSearcher es = new ElementSearcher(params, driverWrapper.getDriver(), cachedWebElements);
		String clearBeforeStr = params.get("clearbefore");
		String textToSend = params.get(TEXT_PARAM);
		boolean clearBefore = "y".equals(clearBeforeStr) || "true".equals(clearBeforeStr);
		
		
		String directSend = params.get("directsend");
		WebElement element = null;
		
		if (StringUtils.isNotEmpty(directSend) && ("y".equals(directSend) || "true".equals(directSend))) {
			logger.debug("Direct sending to control.");
			if (!es.isLocatorsAvailable()) {
				throw new ScriptExecuteException("Locator/Matcher should be specified");
			}
			element = es.searchElement();
			element.sendKeys(textToSend);			
		} else {
			List<String> strings = handler.processInputText(textToSend);
			
			Actions actions = new Actions(driverWrapper.getDriver());
			if (es.isLocatorsAvailable()) {
				element = es.searchElement();
				actions.moveToElement(element);
				actions.click();
			}

			if (clearBefore) {
				actions.sendKeys(Keys.CONTROL, "a", Keys.CONTROL, Keys.BACK_SPACE);
			}

			for (String str : strings) {
				if (handler.needSpecialSend(str)) {
					handler.sendSpecialKey(actions, str, (element instanceof RemoteWebElement ?
							((RemoteWebElement) element).getId() : ""));
				} else {
					handler.doSendKeys(actions, str);
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
}
