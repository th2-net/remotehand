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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Map;

public class GetElementAttribute extends GetElement
{
	private static final String PARAM_ATTRIBUTE = "attribute";

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		String attributeName = params.get(PARAM_ATTRIBUTE);
		if (attributeName == null || attributeName.isEmpty())
			throw new ScriptExecuteException("Param '" + PARAM_ATTRIBUTE + "' cannot be empty");
		
		String attribute = findElement(webDriver, webLocator).getAttribute(attributeName);
		logInfo("Attribute '%s' value: %s.", attributeName, attribute);
		return attribute;
	}
}
