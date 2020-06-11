/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web.webelements;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CssSelector extends WebLocator
{
	@Override
	public By getWebLocator(WebDriver webDriver, String matcher)
	{
		return By.cssSelector(matcher);
	}
	
	@Override
	public By getWebLocator(WebDriver webDriver, Map<String, String> params)
	{
		return getWebLocator(webDriver, params.get(WebLocator.MATCHER));
	}
}
