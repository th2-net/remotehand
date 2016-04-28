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
	public static final String NON_BREAKING_SPACE = "#nbsp#";
	
	@Override
	public By getWebLocator(WebDriver webDriver, String matcher)
	{
		return By.xpath(prepareMatcher(matcher));
	}
	
	@Override
	public By getWebLocator(WebDriver webDriver, Map<String, String> params)
	{
		return getWebLocator(webDriver, params.get(WebLocator.MATCHER));
	}
	
	protected String prepareMatcher(String matcher)
	{
		if (matcher == null)
			return null;
		
		if (matcher.contains(NON_BREAKING_SPACE))
			return matcher.replace(NON_BREAKING_SPACE, "\u00A0");
		else 
			return matcher;
	}
}
