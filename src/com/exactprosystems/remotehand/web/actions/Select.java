/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.remotehand.RhUtils;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Select extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(Select.class);
	private static final String TEXT = "text";
	private static final String DEFAULT_TEXT = "default";
	private static final String NO_OPTION_FAIL_PARAM = "nooptionfail";

	public Select()
	{
		super.mandatoryParams = new String[]{TEXT};
	}

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
		org.openqa.selenium.support.ui.Select dropdown = new org.openqa.selenium.support.ui.Select(webDriver.findElement(webLocator));

		String option = getParams().get(TEXT);
		String default_option = getParams().get(DEFAULT_TEXT);
		if (!doesOptionExist(dropdown, option) && default_option != null && !default_option.isEmpty())
			option = default_option;

		if (doesOptionExist(dropdown, option) || noOptionFail())
		{
			dropdown.selectByVisibleText(option);
			logInfo("Option " + option + " is selected in element with locator " + webLocator);
		} else
		{
			logInfo("Option " + option + " doesn't exist. It hasn't been selected");
		}
		return null;
	}

	private boolean doesOptionExist(org.openqa.selenium.support.ui.Select dropdown, String text)
	{
		for (WebElement option : dropdown.getOptions())
		{
			if (option.getText().equals(text))
				return true;
		}
		return false;
	}

	private boolean noOptionFail()
	{
		return !getParams().containsKey(NO_OPTION_FAIL_PARAM) || RhUtils.YES.contains(getParams().get(NO_OPTION_FAIL_PARAM));
	}

	@Override
	protected Logger getLogger()
	{
		return logger;
	}
}
