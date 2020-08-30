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

import java.util.Map;

public class GetElementAttribute extends WindowsAction {

	private static final Logger logger = LoggerFactory.getLogger(GetElementAttribute.class);

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params) throws ScriptExecuteException {
		ElementSearcher es = new ElementSearcher();
		WebElement element = es.searchElement(params, driverWrapper.getDriver());
		
		String attributeName = params.get("attributename");
		return element.getAttribute(attributeName);
	}
	
	@Override
	public Logger getLoggerInstance() {
		return logger;
	}
}