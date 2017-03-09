////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.ActionOutputType;
import com.exactprosystems.remotehand.web.WebAction;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Map;

/**
 * @author anna.bykova.
 */
public class GetScreenshot extends WebAction
{
	private static final Logger log = Logger.getLogger(GetScreenshot.class);
	
	public static final String NAME_PARAM = "name";

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		return takeScreenshot(params.get(NAME_PARAM));
	}
	
	@Override
	public ActionOutputType getOutputType()
	{
		return ActionOutputType.SCREENSHOT;
	}

	@Override
	public boolean isNeedLocator()
	{
		return false;
	}

	@Override
	public boolean isCanWait()
	{
		return false;
	}
	
	@Override
	protected Logger getLogger()
	{
		return log;
	}
}
