package com.exactprosystems.remotehand.actions;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.exactprosystems.remotehand.Logger;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;

public class SendKeys extends WebAction
{
	private static Logger logger = Logger.getLogger();

	private static enum ActionParams
	{
		text
	};

	public SendKeys()
	{
		super.needLocator = true;

		String[] result = new String[ActionParams.values().length];
		for (int inx = 0; inx < result.length; inx++)
		{
			result[inx] = ActionParams.values()[inx].toString();
		}
		super.mandatoryParams = result;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement input = webDriver.findElement(webLocator);
		input.sendKeys(params.get(ActionParams.text.toString()));

		logger.info("Send text to: " + webLocator);

		return null;
	}
}
