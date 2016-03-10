////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web.webelements;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class XPath extends WebLocator
{
	@Override
	public By getWebLocator(WebDriver webDriver, String matcher)
	{
		return By.xpath(matcher);
	}
	
	@Override
	public By getWebLocator(WebDriver webDriver, Map<String, String> params)
	{
		return getWebLocator(webDriver, params.get(WebLocator.MATCHER));
	}
}
