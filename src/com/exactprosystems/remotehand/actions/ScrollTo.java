////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.actions;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.Locatable;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;

public class ScrollTo extends WebAction
{
	private static final Logger logger = Logger.getLogger(ScrollTo.class);
	
	@Override
	public boolean isNeedLocator()
	{
		return true;
	}
	
	@Override
	public boolean isCanWait()
	{
		return true;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement element = webDriver.findElement(webLocator);

		if (element instanceof Locatable)
		{
			((Locatable)element).getCoordinates().inViewPort();
			logger.info("Locate on: " + webLocator.toString());
		}
		else
			throw new ScriptExecuteException("Can't scroll to the following web element: '" + element.toString() + "'");

		return null;
	}

}
