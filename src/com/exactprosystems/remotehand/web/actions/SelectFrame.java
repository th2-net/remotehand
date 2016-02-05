////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


import java.util.Map;

/**
 * Created by alexey.karpukhin on 1/20/16.
 */
public class SelectFrame extends WebAction {

	private static final Logger logger = Logger.getLogger(SelectFrame.class);

	@Override
	public boolean isNeedLocator() {
		return false;
	}

	@Override
	public boolean isCanWait() {
		return true;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {

		if (webLocator == null) {
			logger.info("Selecting default frame.");
			webDriver.switchTo().defaultContent();
		} else {
			WebElement element = webDriver.findElement(webLocator);
			logger.info("Selecting frame: " + element.getAttribute("name"));
			webDriver.switchTo().frame(element);
		}

		return null;
	}
}
