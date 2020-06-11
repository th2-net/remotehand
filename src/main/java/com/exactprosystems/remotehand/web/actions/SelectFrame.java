/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by alexey.karpukhin on 1/20/16.
 */
public class SelectFrame extends WebAction {

	private static final Logger logger = LoggerFactory.getLogger(SelectFrame.class);

	@Override
	public boolean isNeedLocator() {
		return false;
	}

	@Override
	public boolean isCanWait() {
		return true;
	}

	@Override
	protected Logger getLogger()
	{
		return logger;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {

		if (webLocator == null) {
			logInfo("Selecting default frame.");
			webDriver.switchTo().defaultContent();
		} else {
			WebElement element = webDriver.findElement(webLocator);
			logInfo("Selecting frame: " + element.getAttribute("name"));
			webDriver.switchTo().frame(element);
		}

		return null;
	}
}
