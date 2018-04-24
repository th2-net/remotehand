/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
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

public abstract class WebLocator
{
	protected static final String MATCHER = "matcher";

	public abstract By getWebLocator(WebDriver webDriver, String matcher);
	public abstract By getWebLocator(WebDriver webDriver, Map<String, String> params);
	
	public String[] getMandatoryParams()
	{
		final String[] result = { MATCHER };

		return result;
	}
}
