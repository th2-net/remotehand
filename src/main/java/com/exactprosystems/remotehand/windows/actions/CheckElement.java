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
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CheckElement extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(CheckElement.class);

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params) throws ScriptExecuteException {

		ElementSearcher es = new ElementSearcher();
		List<? extends WebElement> element = es.searchElementsWithoutWait(params, driverWrapper.getDriver());

		String attributeName = params.get("attributename");
		if (attributeName != null && element != null && element.size() > 0) {
			return element.get(0).getAttribute(attributeName);
		} else if (element != null && element.size() > 0) {
			return "found";
		}

		return "not found";
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
