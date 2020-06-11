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

public class DurationStart extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(DurationStart.class);
	protected static final String PARAM_ID = "id",
			CONTEXT_LAST_DURATION_START = "LastDurationStart";
	
	@Override
	protected Logger getLogger()
	{
		return logger;
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
	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return new String[] {PARAM_ID};
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		Map<String, Object> contextData = context.getContextData();
		Long start = System.currentTimeMillis();
		contextData.put(buildDurationStartId(params.get(PARAM_ID)), start);
		contextData.put(CONTEXT_LAST_DURATION_START, start);
		return null;
	}
	
	
	public static String buildDurationStartId(String id)
	{
		return "DurationStart_"+id;
	}
}