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

import com.exactprosystems.remotehand.RhUtils;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author anna.bykova.
 */
public class SetCheckbox extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(SetCheckbox.class);
	
	public static final String PARAM_CHECKED = "checked",
			STATE_CHECKED = "checked", STATE_UNCHECKED = "unchecked";

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
	protected Logger getLogger()
	{
		return logger;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement checkbox = findElement(webDriver, webLocator);
		boolean shouldBeChecked = !RhUtils.NO.contains(params.get(PARAM_CHECKED));
		switchCheckbox(checkbox, shouldBeChecked);
		return null;
	}
	
	private void switchCheckbox(WebElement checkbox, boolean shouldBeChecked) throws ScriptExecuteException
	{
		String newState = shouldBeChecked ? STATE_CHECKED : STATE_UNCHECKED;

		boolean checkedNow = isCheckboxChecked(checkbox);

		if (shouldBeChecked == checkedNow)
		{
			logInfo("Checkbox is already in required state: %s", newState);
			return;
		}
		
		try
		{
			checkbox.click();
		}
		catch (WebDriverException e)
		{
			if (e.getMessage().contains("Element is not clickable") && checkbox.isSelected() != shouldBeChecked) // Case from LCH frontend
			{
				WebElement parent = checkbox.findElement(By.xpath(".."));
				logInfo("Trying to click on parent element: %s", parent);
				parent.click();
				if (checkbox.isSelected() != shouldBeChecked)
					throw new ScriptExecuteException(String.format("Cannot change state of checkbox '%s'", checkbox), e);
			}
			else
			{
				throw e;
			}
		}
		logInfo("Checkbox has been successfully updated, new state: %s", newState);
	}

	protected boolean isCheckboxChecked(WebElement checkbox)
	{
		String type = checkbox.getAttribute("type");
		String clazz = checkbox.getAttribute("class");
		if ("checkbox".equalsIgnoreCase(type) || clazz == null)
			return checkbox.isSelected();
		else
			return clazz.contains(STATE_CHECKED) && !clazz.contains(STATE_UNCHECKED);
	}
}
