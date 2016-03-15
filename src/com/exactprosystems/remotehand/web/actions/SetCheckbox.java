////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
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

/**
 * @author anna.bykova.
 */
public class SetCheckbox extends WebAction
{
	private static final Logger logger = Logger.getLogger(SetCheckbox.class);
	
	public static final String PARAM_CHECKED = "checked";

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
		WebElement checkbox = webDriver.findElement(webLocator);
		boolean shouldBeChecked = !WebScriptCompiler.NO.contains(params.get(PARAM_CHECKED));
		switchCheckbox(checkbox, shouldBeChecked);	
		return null;
	}
	
	private void switchCheckbox(WebElement checkbox, boolean newState) throws ScriptExecuteException
	{
		String newStateStr = newState ? "selected" : "cleared";
		boolean oldState = checkbox.isSelected();
		if (newState == oldState)
		{
			logger.info("Checkbox has been already " + newStateStr);
			return;
		}
		
		checkbox.click();
		if (checkbox.isSelected() != newState) // Case from LCH frontend
		{
			WebElement parent = checkbox.findElement(By.xpath(".."));
			logger.info("Try to click on parent " + parent);
			parent.click();
			if (checkbox.isSelected() != newState)
				throw new ScriptExecuteException("Cannot change state of checkbox " + checkbox);				
		}
		logger.info("Checkbox has been successfully " + newStateStr);
	}
}
