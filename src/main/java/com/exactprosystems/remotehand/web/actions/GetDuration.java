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

public class GetDuration extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(GetDuration.class);
	protected static final String PARAM_STARTID = "startid",
			PARAM_NAME = "name",
			CONTEXT_LAST_GET_DURATION = "LastGetDuration";
	
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
		return logger;
	}
	
	@Override
	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return new String[] {PARAM_NAME, PARAM_STARTID};
	}
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		Long start = null,
				end = System.currentTimeMillis();
		
		String id = params.get(PARAM_STARTID);
		start = (Long)context.getContextData().get(DurationStart.buildDurationStartId(id));
		if (start == null)
			throw new ScriptExecuteException("No 'DurationStart' action executed with ID='"+id+"'");
		
		return "Duration "+params.get(PARAM_NAME)+": "+(end-start);
	}
}