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
import com.exactprosystems.remotehand.windows.ElementSearcher;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext.CachedWebElements;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SearchElement extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(SearchElement.class);
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, CachedWebElements cachedWebElements) throws ScriptExecuteException {
		if (getId() == null) {
			throw new ScriptExecuteException("Id is not specified");
		}

		ElementSearcher elementSearcher = new ElementSearcher(params, driverWrapper.getDriver(), cachedWebElements);
		WebElement webElement = elementSearcher.searchElement();
		cachedWebElements.storeWebElement(getId(), webElement);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Stored element to cached. RH_id: {} Win_id: {}", getId(), 
					(webElement instanceof RemoteWebElement) ? ((RemoteWebElement) webElement).getId() : "");
		}
		
		return null;
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
