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
import com.exactprosystems.remotehand.web.WebAction;
import com.exactprosystems.remotehand.web.WebScriptCompiler;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Map;

public class Select extends WebAction
{
	private static final Logger logger = Logger.getLogger(Select.class);
	private static final String TEXT = "text";
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
		if (doesOptionExist(dropdown, getParams().get(TEXT)) || noOptionFail())
		{
			dropdown.selectByVisibleText(params.get(TEXT));
			logInfo("Option " + params.get(TEXT) + " is selected in element with locator " + webLocator);
		} else
		{
			logInfo("Option " + getParams().get(TEXT) + " doesn't exist. It hasn't been selected");
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
		return !getParams().containsKey(NO_OPTION_FAIL_PARAM) || WebScriptCompiler.YES.contains(getParams().get(NO_OPTION_FAIL_PARAM));
	}

	@Override
	protected Logger getLogger()
	{
		return logger;
	}
}
