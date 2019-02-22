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

import java.util.Arrays;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

public class WaitForChanges extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(WaitForChanges.class);
	private static final String PARAM_SECONDS = "seconds",
			PARAM_SCREENSHOTID = "screenshotid",
			PARAM_CHECKMILLIS = "checkmillis";
	
	@Override
	public boolean isNeedLocator()
	{
		return true;
	}
	
	@Override
	public boolean isCanWait()
	{
		return false;
	}
	
	@Override
	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return new String[] {PARAM_SECONDS, PARAM_SCREENSHOTID, PARAM_CHECKMILLIS};
	}
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		int seconds = getIntegerParam(params, PARAM_SECONDS),
				checkMillis = getIntegerParam(params, PARAM_CHECKMILLIS);
		String id = params.get(PARAM_SCREENSHOTID);
		
		byte[] initialState = (byte[])context.getContextData().get(GetElementScreenshot.buildScreenshotId(id));
		if (initialState == null)
			throw new ScriptExecuteException("No screenshot stored for ID='"+id+"'");
		
		long endTime = System.currentTimeMillis()+seconds;
		do
		{
			byte[] currentState = takeElementScreenshot(webDriver, webLocator);
			if (!compareStates(initialState, currentState))
				return null;
			
			if (System.currentTimeMillis() >= endTime)
				break;
			
			try
			{
				Thread.sleep(checkMillis);
			}
			catch (InterruptedException e)
			{
				// do nothing like in WaitForNew
			}
		}
		while (true);

		throw new ScriptExecuteException("No changes caught in element during "+seconds+" seconds");
	}
	
	@Override
	protected Logger getLogger()
	{
		return logger;
	}
	
	
	private boolean compareStates(byte[] state1, byte[] state2)
	{
		return Arrays.equals(state1, state2);
	}
}