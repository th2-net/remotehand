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

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebScriptCompiler;
import com.exactprosystems.remotehand.web.webelements.WebLocatorsMapping;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ScrollDivTo extends ScrollTo
{
	private static final Logger logger = LoggerFactory.getLogger(ScrollDivTo.class);

	public static final String
			PARAM_LOCATOR2 = String.format("%s2", WebScriptCompiler.WEB_LOCATOR),
			PARAM_MATCHER2 = String.format("%s2", WebScriptCompiler.WEB_MATCHER),
			PARAM_Y_OFFSET = "yoffset";

	@Override
	protected Logger getLogger()
	{
		return logger;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement divWithScrollbar = webDriver.findElement(webLocator);
		By webLocator2;
		WebElement elementToScrollTo;
		try
		{
			String locator2 = params.get(PARAM_LOCATOR2), matcher2 = params.get(PARAM_MATCHER2);
			webLocator2 = WebLocatorsMapping.getInstance().getByName(locator2).getWebLocator(webDriver, matcher2);
			elementToScrollTo = webDriver.findElement(webLocator2);
		}
		catch (ScriptCompileException e)
		{
			throw new ScriptExecuteException("Error while resolving locator2", e);
		}

		int yOffset = Integer.parseInt(params.getOrDefault(PARAM_Y_OFFSET, "0"));
		JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
		jsExecutor.executeScript("arguments[0].scrollTop=arguments[1].offsetTop", divWithScrollbar, elementToScrollTo);
		jsExecutor.executeScript("arguments[0].scrollTop+=(arguments[2]))", divWithScrollbar, yOffset);
		logInfo("Element '%s' located on element '%s' with y-offset %s", webLocator, webLocator2, yOffset);

		return null;
	}
}
