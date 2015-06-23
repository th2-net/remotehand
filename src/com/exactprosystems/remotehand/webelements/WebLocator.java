////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.webelements;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public abstract class WebLocator
{
	protected static final String MATCHER = "matcher";

	public abstract By getWebLocator(WebDriver webDriver, Map<String, String> params);

	public String[] getMandatoryParams()
	{
		final String[] result = { MATCHER };

		return result;
	}
}
