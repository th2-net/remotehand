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
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ToggleCheckBox extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(ToggleCheckBox.class);
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedWebElements) throws ScriptExecuteException {

		ElementSearcher es = new ElementSearcher();
		WebElement element = es.searchElement(params, driverWrapper.getDriver(), cachedWebElements);
		
		String expectedState = params.get("expectedstate");
		expectedState = expectedState != null ? expectedState.toLowerCase() : null;
		boolean checked;
		if ("checked".equals(expectedState) || "true".equals(expectedState)) {
			checked = true;
		} else if ("unchecked".equals(expectedState) || "false".equals(expectedState)) {
			checked = false;
		} else {
			throw new ScriptExecuteException("Unknown expectedState = " + expectedState);
		}

		String toggleState = element.getAttribute("Toggle.ToggleState");
		this.logger.debug("Element source toggle state : {}", toggleState);
		
		if (toggleState != null && (("on".equalsIgnoreCase(toggleState) || "1".equals(toggleState)) && !checked) 
				|| (("off".equalsIgnoreCase(toggleState) || "0".equals(toggleState) && checked))) {
			element.click();
			this.logger.debug("RH clicked on this checkbox");
		} else {
			this.logger.debug("None of actions should be provided");
		}
		
		return null;

	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
