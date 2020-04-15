/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.exactprosystems.remotehand.web.WebUtils.waitForAlert;
import static com.exactprosystems.remotehand.web.actions.AcceptAlert.WAIT_PARAM;

public class DismissAlert extends WebAction
{
	private static final Logger log = LoggerFactory.getLogger(DismissAlert.class);
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		int wait = getIntegerParam(params, WAIT_PARAM);
		Alert alert = waitForAlert(webDriver, wait);
		try
		{
			alert.dismiss();
		}
		catch (Exception e)
		{
			throw new ScriptExecuteException("Unable to dismiss alert.", e);
		}
		return null;
	}

	@Override
	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return new String[]{WAIT_PARAM};
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
