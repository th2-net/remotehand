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

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class GetElementScreenshot extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(GetElementScreenshot.class);
	
	private static final String PARAM_ID = "id";
	
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
	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return new String[] {PARAM_ID};
	}
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		byte[] screen = takeElementScreenshot(webDriver, webLocator);
		context.getContextData().put(buildScreenshotId(params.get(PARAM_ID)), screen);
		return null;
	}
	
	@Override
	protected Logger getLogger()
	{
		return logger;
	}
	
	
	public static String buildScreenshotId(String id)
	{
		return "Screenshot_"+id;
	}
}