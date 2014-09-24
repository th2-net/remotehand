package com.exactprosystems.remotehand.actions;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.exactprosystems.remotehand.Logger;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;

public class SendKeys extends WebAction
{
	private static final Logger logger = Logger.getLogger();
	private static final String PARAM_TEXT = "text";

	public SendKeys()
	{
		super.mandatoryParams = new String[]{PARAM_TEXT};
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
		WebElement input = webDriver.findElement(webLocator);
		String text = params.get(PARAM_TEXT);
		if (text.endsWith("\\r\\n"))
			text = text.substring(0, text.length()-4)+Keys.RETURN.toString();
		input.sendKeys(text);
		logger.info("Sent text to: " + webLocator);

		return null;
	}
}
